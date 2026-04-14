package com.nss.nssreport.util;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SftpDownloader {

    private final String HOST = "10.255.0.43";
    private final int PORT = 22;
    private final String USER = "gkkuser";
    private final String PASSWORD = "lB6TqUo8K9Atg1asuFBC!";

    private final String REMOTE_DIR = "/opt/oss/server/var/fileint/pm/";
    private final String FILE_MASK = "83888355";

    public List<String> downloadLatestFiles(String localDir) throws Exception {
        List<String> downloaded = new ArrayList<>();

        JSch jsch = new JSch();
        Session session = jsch.getSession(USER, HOST, PORT);
        session.setPassword(PASSWORD);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "password");
        session.setConfig("compression.s2c", "none");
        session.setConfig("compression.c2s", "none");
        session.setServerAliveInterval(5000);
        session.connect(30000);

        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(10000);

            // Находим последнюю папку pmexport_XXXXXXXX
            List<String> dirs = new ArrayList<>();
            sftp.ls(REMOTE_DIR, entry -> {
                if (entry.getFilename().startsWith("pmexport_")) {
                    dirs.add(entry.getFilename());
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            });

            if (dirs.isEmpty()) throw new Exception("Папки pmexport не найдены");

            String latestDir = dirs.stream().sorted().reduce((a, b) -> b).get();
            String remoteFullPath = REMOTE_DIR + latestDir + "/";
            System.out.println("Читаем папку: " + remoteFullPath);

            // Получаем список файлов
            List<String> fileNames = new ArrayList<>();
            sftp.ls(remoteFullPath, entry -> {
                String name = entry.getFilename();
                if (name.contains(FILE_MASK) && name.endsWith(".csv")) {
                    fileNames.add(name);
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            });

            // Скачиваем каждый файл отдельным каналом
            for (String name : fileNames) {
                String remotePath = remoteFullPath + name;
                String localPath = localDir + "/" + name;
                System.out.println("Скачиваем: " + name);

                // Открываем новый канал для каждого файла
                ChannelSftp ch = (ChannelSftp) session.openChannel("sftp");
                ch.connect(10000);
                try (InputStream in = ch.get(remotePath);
                     FileOutputStream out = new FileOutputStream(localPath);
                     BufferedOutputStream bos = new BufferedOutputStream(out, 131072)) {
                    byte[] buf = new byte[131072]; // 128KB
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                    }
                    bos.flush();
                } finally {
                    ch.disconnect();
                }

                downloaded.add(localPath);
                System.out.println("Скачан: " + name);
            }

        } finally {
            if (sftp != null && sftp.isConnected()) sftp.disconnect();
            if (session.isConnected()) session.disconnect();
        }

        return downloaded;
    }
}