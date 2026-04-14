package com.nss.nssreport.util;


import com.nss.nssreport.domain.dto.AsrDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelParser {

    // Метод принимает путь к файлу — в будущем можно менять на любой путь
    public List<AsrDto> parseAsrKpi(String filePath) throws Exception {

        List<AsrDto> result = new ArrayList<>();

        // Открываем файл по пути
        InputStream is = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            AsrDto dto = new AsrDto();

            dto.setObjectInstance(getString(row, 0));
            dto.setDate(getDate(row, 28));
            dto.setTime(getTime(row, 29));
            dto.setCallAttempt(getLong(row, 3));
            dto.setAnswerTimes(getLong(row, 6));
            dto.setAbandonBeforeRing(getLong(row, 14));
            dto.setAbandonAfterRing(getLong(row, 15));
            dto.setRingedNoAnswer(getLong(row, 16));
            dto.setUserDetBusy(getLong(row, 17));
            dto.setUserBusy(getLong(row, 18));
            dto.setInvalidAddress(getLong(row, 19));
            dto.setPagingNoResponse(getLong(row, 20));
            dto.setAbsentSubscriber(getLong(row, 21));
            dto.setServiceRestricted(getLong(row, 22));
            dto.setSpecialSignal(getLong(row, 23));
            dto.setCalledNoRespond(getLong(row, 26));

            long callAttempt = safe(dto.getCallAttempt());
            long answer = safe(dto.getAnswerTimes());

            long sucAttempt = answer
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

            dto.setSucAttempt(sucAttempt);
            dto.setAsr(callAttempt > 0 ? (double) answer / callAttempt * 100 : 0);
            dto.setNer(callAttempt > 0 ? (double) sucAttempt / callAttempt * 100 : 0);

            result.add(dto);
        }

        workbook.close();
        return result;
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return cell.toString();
    }

    private Long getLong(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return 0L;
        try {
            return (long) cell.getNumericCellValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    private LocalDate getDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime getTime(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        } catch (Exception e) {
            return null;
        }
    }

    private long safe(Long value) {
        return value != null ? value : 0L;
    }
}
