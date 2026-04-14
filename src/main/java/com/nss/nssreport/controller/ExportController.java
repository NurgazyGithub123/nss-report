package com.nss.nssreport.controller;

import com.nss.nssreport.domain.dto.AlertDto;
import com.nss.nssreport.domain.dto.DailyAvgDto;
import com.nss.nssreport.service.AlertService;
import com.nss.nssreport.service.AsrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Export", description = "Выгрузка данных в Excel и CSV")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final AsrService asrService;
    private final AlertService alertService;

    // ==================== EXCEL ====================

    @Operation(summary = "Выгрузить агрегированные данные в Excel")
    @GetMapping("/daily/excel")
    public ResponseEntity<byte[]> exportDailyExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws Exception {

        List<DailyAvgDto> data = asrService.getDailyAvgByDateBetween(from, to);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("ASR Daily");

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] cols = {"Дата", "Маршрут", "ASR %", "NER %",
                    "Всего звонков", "Отвечено", "Успешных"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (DailyAvgDto d : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(d.getDate().toString());
                row.createCell(1).setCellValue(d.getObjectInstance());
                row.createCell(2).setCellValue(d.getAsr());
                row.createCell(3).setCellValue(d.getNer());
                row.createCell(4).setCellValue(d.getTotalCallAttempt());
                row.createCell(5).setCellValue(d.getTotalAnswer());
                row.createCell(6).setCellValue(d.getTotalSucAttempt());
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=asr_daily_" + from + "_" + to + ".xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    @Operation(summary = "Выгрузить алерты в Excel")
    @GetMapping("/alerts/excel")
    public ResponseEntity<byte[]> exportAlertsExcel() throws Exception {

        List<AlertDto> data = alertService.findAsrDropAlerts();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Alerts");

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] cols = {"Маршрут", "Дата", "ASR последний", "ASR средний",
                    "Падение ASR", "NER последний", "NER средний",
                    "Падение NER", "Статус"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle redStyle = wb.createCellStyle();
            redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle yellowStyle = wb.createCellStyle();
            yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle greenStyle = wb.createCellStyle();
            greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 1;
            for (AlertDto a : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.getObjectInstance());
                row.createCell(1).setCellValue(a.getLastDate().toString());
                row.createCell(2).setCellValue(a.getLastAsr());
                row.createCell(3).setCellValue(a.getAvgAsr());
                row.createCell(4).setCellValue(a.getDropAsr());
                row.createCell(5).setCellValue(a.getLastNer());
                row.createCell(6).setCellValue(a.getAvgNer());
                row.createCell(7).setCellValue(a.getDropNer());

                Cell statusCell = row.createCell(8);
                statusCell.setCellValue(a.getStatus());
                if (a.getStatus().contains("ALERT")) {
                    statusCell.setCellStyle(redStyle);
                } else if (a.getStatus().contains("WARN")) {
                    statusCell.setCellStyle(yellowStyle);
                } else {
                    statusCell.setCellStyle(greenStyle);
                }
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=alerts_" + LocalDate.now() + ".xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    // ==================== CSV ====================

    @Operation(summary = "Выгрузить агрегированные данные в CSV")
    @GetMapping("/daily/csv")
    public ResponseEntity<byte[]> exportDailyCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<DailyAvgDto> data = asrService.getDailyAvgByDateBetween(from, to);

        StringBuilder sb = new StringBuilder();
        sb.append("Дата,Маршрут,ASR %,NER %,Всего звонков,Отвечено,Успешных\n");
        for (DailyAvgDto d : data) {
            sb.append(d.getDate()).append(",")
                    .append(d.getObjectInstance()).append(",")
                    .append(String.format("%.2f", d.getAsr())).append(",")
                    .append(String.format("%.2f", d.getNer())).append(",")
                    .append(d.getTotalCallAttempt()).append(",")
                    .append(d.getTotalAnswer()).append(",")
                    .append(d.getTotalSucAttempt()).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=asr_daily_" + from + "_" + to + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @Operation(summary = "Выгрузить алерты в CSV")
    @GetMapping("/alerts/csv")
    public ResponseEntity<byte[]> exportAlertsCsv() {

        List<AlertDto> data = alertService.findAsrDropAlerts();

        StringBuilder sb = new StringBuilder();
        sb.append("Маршрут,Дата,ASR последний,ASR средний,Падение ASR," +
                "NER последний,NER средний,Падение NER,Статус\n");
        for (AlertDto a : data) {
            sb.append(a.getObjectInstance()).append(",")
                    .append(a.getLastDate()).append(",")
                    .append(String.format("%.2f", a.getLastAsr())).append(",")
                    .append(String.format("%.2f", a.getAvgAsr())).append(",")
                    .append(String.format("%.2f", a.getDropAsr())).append(",")
                    .append(String.format("%.2f", a.getLastNer())).append(",")
                    .append(String.format("%.2f", a.getAvgNer())).append(",")
                    .append(String.format("%.2f", a.getDropNer())).append(",")
                    .append(a.getStatus()).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=alerts_" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}