package com.nss.nssreport.controller;

import com.jcraft.jsch.*;
import com.nss.nssreport.config.AsrConfig;
import com.nss.nssreport.domain.dto.AsrDto;
import com.nss.nssreport.domain.dto.DailyAvgDto;
import com.nss.nssreport.service.AsrDailyService;
import com.nss.nssreport.service.AsrService;
import com.nss.nssreport.util.CsvParser;
import com.nss.nssreport.util.ExcelParser;
import com.nss.nssreport.util.SftpDownloader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "ASR KPI", description = "Управление данными ASR и NER")
@RestController
@RequestMapping("/api/asr")
@RequiredArgsConstructor
public class AsrController {

    private final AsrService asrService;
    private final AsrDailyService dailyService;
    private final ExcelParser excelParser;
    private final AsrConfig asrConfig;
    private final SftpDownloader sftpDownloader;
    private final CsvParser csvParser;

    @Operation(summary = "Загрузить Excel файл")
    @PostMapping("/upload")
    public ResponseEntity<String> upload() {
        try {
            String path = asrConfig.getExcelPath();
            List<AsrDto> dtos = excelParser.parseAsrKpi(path);
            asrService.saveAll(dtos);
            return ResponseEntity.ok("✅ Сохранено записей: " + dtos.size());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    @Operation(summary = "Загрузить CSV с сервера по SFTP (последняя папка = вчерашний день)")
    @PostMapping("/upload-sftp")
    public ResponseEntity<String> uploadFromSftp() {
        try {
            String tmpDir = System.getProperty("java.io.tmpdir") + "/nss_csv";
            new java.io.File(tmpDir).mkdirs();

            List<String> files = sftpDownloader.downloadLatestFiles(tmpDir);

            int total = 0;
            for (String filePath : files) {
                List<AsrDto> dtos = csvParser.parseCsv(filePath);
                asrService.saveAll(dtos);
                total += dtos.size();
            }

            // Агрегируем вчерашний день в asr_daily
            LocalDate yesterday = LocalDate.now().minusDays(1);
            dailyService.aggregateAndSave(yesterday);

            return ResponseEntity.ok("✅ Скачано файлов: " + files.size() +
                    ", сохранено записей: " + total +
                    ", агрегировано за: " + yesterday);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    @Operation(summary = "Загрузить все папки (разовая загрузка)")
    @PostMapping("/upload-sftp-all")
    public ResponseEntity<String> uploadFromSftpAll() {
        try {
            String tmpDir = System.getProperty("java.io.tmpdir") + "/nss_csv";
            new java.io.File(tmpDir).mkdirs();

            JSch jsch = new JSch();
            Session session = jsch.getSession("gkkuser", "10.255.0.43", 22);
            session.setPassword("lB6TqUo8K9Atg1asuFBC!");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("compression.s2c", "none");
            session.setConfig("compression.c2s", "none");
            session.connect(30000);

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(10000);

            List<String> dirs = new ArrayList<>();
            sftp.ls("/opt/oss/server/var/fileint/pm/", entry -> {
                if (entry.getFilename().startsWith("pmexport_")) {
                    dirs.add(entry.getFilename());
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            });
            dirs.sort(String::compareTo);

            int totalFiles = 0;
            int totalRecords = 0;

            for (String dir : dirs) {
                String remoteFullPath = "/opt/oss/server/var/fileint/pm/" + dir + "/";

                List<String> fileNames = new ArrayList<>();
                sftp.ls(remoteFullPath, entry -> {
                    String name = entry.getFilename();
                    if (name.contains("83888355") && name.endsWith(".csv")) {
                        fileNames.add(name);
                    }
                    return ChannelSftp.LsEntrySelector.CONTINUE;
                });

                for (String name : fileNames) {
                    String localPath = tmpDir + "/" + name;
                    ChannelSftp ch = (ChannelSftp) session.openChannel("sftp");
                    ch.connect(10000);
                    try (java.io.InputStream in = ch.get(remoteFullPath + name);
                         java.io.FileOutputStream out = new java.io.FileOutputStream(localPath);
                         java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(out, 131072)) {
                        byte[] buf = new byte[131072];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            bos.write(buf, 0, len);
                        }
                        bos.flush();
                    } finally {
                        ch.disconnect();
                    }

                    List<AsrDto> dtos = csvParser.parseCsv(localPath);
                    asrService.saveAll(dtos);
                    totalRecords += dtos.size();
                    totalFiles++;
                    System.out.println("Загружен: " + name + " (" + dtos.size() + " записей)");
                }
            }

            sftp.disconnect();
            session.disconnect();

            // Агрегируем все дни в asr_daily
            dailyService.aggregateAll();

            return ResponseEntity.ok("✅ Папок: " + dirs.size() +
                    ", файлов: " + totalFiles +
                    ", записей: " + totalRecords +
                    ", агрегация завершена");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    @Operation(summary = "Получить данные с фильтрами")
    @GetMapping
    public ResponseEntity<List<AsrDto>> get(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String route,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<AsrDto> result;

        if (from != null && to != null && route != null) {
            result = asrService.getByDateBetweenAndObjectInstance(from, to, route);
        } else if (from != null && to != null) {
            result = asrService.getByDateBetween(from, to);
        } else if (date != null && route != null) {
            result = asrService.getByDateAndObjectInstance(date, route);
        } else if (date != null) {
            result = asrService.getByDate(date);
        } else if (route != null) {
            result = asrService.getByObjectInstance(route);
        } else {
            result = asrService.getAll();
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Суточная агрегация ASR и NER")
    @GetMapping("/daily")
    public ResponseEntity<List<DailyAvgDto>> getDailyAvg(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (from != null && to != null) {
            return ResponseEntity.ok(asrService.getDailyAvgByDateBetween(from, to));
        } else if (date != null) {
            return ResponseEntity.ok(asrService.getDailyAvgByDate(date));
        }
        return ResponseEntity.badRequest().build();
    }

    // DELETE http://localhost:8080/api/asr/all
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll() {
        asrService.deleteAll();
        return ResponseEntity.ok("✅ Все записи удалены!");
    }

    // DELETE http://localhost:8080/api/asr/date?date=2026-01-20
    @DeleteMapping("/date")
    public ResponseEntity<String> deleteByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        asrService.deleteByDate(date);
        return ResponseEntity.ok("✅ Записи за " + date + " удалены!");
    }

    // DELETE http://localhost:8080/api/asr/route?route=CHINA(8),CHINA(8)
    @DeleteMapping("/route")
    public ResponseEntity<String> deleteByRoute(
            @RequestParam String route) {
        asrService.deleteByObjectInstance(route);
        return ResponseEntity.ok("✅ Записи маршрута " + route + " удалены!");
    }
}