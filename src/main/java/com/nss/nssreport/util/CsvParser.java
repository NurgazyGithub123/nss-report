package com.nss.nssreport.util;

import com.nss.nssreport.domain.dto.AsrDto;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser {

    private static final int COL_RESULT_TIME        = 0;
    private static final int COL_OBJECT_NAME        = 2;
    private static final int COL_CALL_ATTEMPT       = 4;  // 84163900
    private static final int COL_ANSWER_TIMES       = 7;  // 84163903
    private static final int COL_ASR_PCT            = 8;  // 84163904
    private static final int COL_NER_PCT            = 9;  // 84163905
    private static final int COL_ABANDON_BEF_RING   = 15; // 84163912
    private static final int COL_ABANDON_AFT_RING   = 16; // 84163913
    private static final int COL_RINGED_NO_ANSWER   = 17; // 84163914
    private static final int COL_USER_DET_BUSY      = 18; // 84163915
    private static final int COL_USER_BUSY          = 19; // 84163916
    private static final int COL_INVALID_ADDRESS    = 20; // 84163917
    private static final int COL_PAGING_NO_RESP     = 21; // 84163918
    private static final int COL_ABSENT_SUBSCRIBER  = 22; // 84163919
    private static final int COL_SERVICE_RESTRICTED = 23; // 84163920
    private static final int COL_SPECIAL_SIGNAL     = 24; // 84163921
    private static final int COL_CALLED_NO_RESPOND  = 27; // 84163924

    public List<AsrDto> parseCsv(String filePath) throws Exception {
        List<AsrDto> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum <= 2) continue; // строка 1 — заголовки, строка 2 — единицы
                if (line.isBlank()) continue;

                String[] cols = splitCsvLine(line);

                try {
                    long callAttempt = parseLong(cols, COL_CALL_ATTEMPT);
                    if (callAttempt == 0) continue; // пропускаем нулевые строки

                    AsrDto dto = new AsrDto();

                    String dateTime = clean(cols[COL_RESULT_TIME]);
                    String[] dtParts = dateTime.split(" ");
                    dto.setDate(LocalDate.parse(dtParts[0]));
                    dto.setTime(LocalTime.parse(dtParts[1]));

                    dto.setObjectInstance(extractRoute(clean(cols[COL_OBJECT_NAME])));

                    dto.setCallAttempt(callAttempt);
                    dto.setAnswerTimes(parseLong(cols, COL_ANSWER_TIMES));
                    dto.setAbandonBeforeRing(parseLong(cols, COL_ABANDON_BEF_RING));
                    dto.setAbandonAfterRing(parseLong(cols, COL_ABANDON_AFT_RING));
                    dto.setRingedNoAnswer(parseLong(cols, COL_RINGED_NO_ANSWER));
                    dto.setUserDetBusy(parseLong(cols, COL_USER_DET_BUSY));
                    dto.setUserBusy(parseLong(cols, COL_USER_BUSY));
                    dto.setInvalidAddress(parseLong(cols, COL_INVALID_ADDRESS));
                    dto.setPagingNoResponse(parseLong(cols, COL_PAGING_NO_RESP));
                    dto.setAbsentSubscriber(parseLong(cols, COL_ABSENT_SUBSCRIBER));
                    dto.setServiceRestricted(parseLong(cols, COL_SERVICE_RESTRICTED));
                    dto.setSpecialSignal(parseLong(cols, COL_SPECIAL_SIGNAL));
                    dto.setCalledNoRespond(parseLong(cols, COL_CALLED_NO_RESPOND));

                    long suc = safe(dto.getAnswerTimes())
                            + safe(dto.getAbandonBeforeRing())
                            + safe(dto.getAbandonAfterRing())
                            + safe(dto.getRingedNoAnswer())
                            + safe(dto.getUserDetBusy())
                            + safe(dto.getUserBusy())
                            + safe(dto.getInvalidAddress())
                            + safe(dto.getPagingNoResponse())
                            + safe(dto.getAbsentSubscriber())
                            + safe(dto.getServiceRestricted())
                            + safe(dto.getSpecialSignal())
                            + safe(dto.getCalledNoRespond());
                    dto.setSucAttempt(suc);

                    // ASR и NER берём из файла — уже посчитаны оборудованием
                    dto.setAsr(parseDouble(cols, COL_ASR_PCT));
                    dto.setNer(parseDouble(cols, COL_NER_PCT));

                    result.add(dto);

                } catch (Exception e) {
                    System.err.println("Строка " + lineNum + " пропущена: " + e.getMessage());
                }
            }
        }

        System.out.println("Загружено из " + filePath + ": " + result.size() + " записей");
        return result;
    }

    // "MSX-Bishkek/...CDCGN=RUSSIA_MTS, CDCN=..." → "RUSSIA_MTS"
    private String extractRoute(String objectName) {
        if (objectName.contains("CDCGN=")) {
            String after = objectName.split("CDCGN=")[1];
            return after.split("[,/]")[0].trim();
        }
        return objectName;
    }

    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private String clean(String s) {
        return s.replaceAll("^\"|\"$", "").trim();
    }

    private long parseLong(String[] cols, int idx) {
        if (idx >= cols.length) return 0L;
        try {
            String val = clean(cols[idx]);
            if (val.isEmpty()) return 0L;
            return (long) Double.parseDouble(val);
        } catch (Exception e) {
            return 0L;
        }
    }

    private double parseDouble(String[] cols, int idx) {
        if (idx >= cols.length) return 0.0;
        try {
            String val = clean(cols[idx]);
            if (val.isEmpty()) return 0.0;
            return Double.parseDouble(val);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private long safe(Long value) {
        return value != null ? value : 0L;
    }
}