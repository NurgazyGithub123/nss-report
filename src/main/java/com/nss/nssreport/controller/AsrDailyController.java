package com.nss.nssreport.controller;

import com.nss.nssreport.domain.entity.AsrDailyEntity;
import com.nss.nssreport.repository.AsrDailyRepository;
import com.nss.nssreport.service.AsrDailyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "ASR Daily", description = "Агрегированные данные по дням")
@RestController
@RequestMapping("/api/daily")
@RequiredArgsConstructor
public class AsrDailyController {

    private final AsrDailyService dailyService;
    private final AsrDailyRepository dailyRepository;

    @Operation(summary = "Агрегировать все данные из asr_kpi в asr_daily")
    @PostMapping("/aggregate-all")
    public ResponseEntity<String> aggregateAll() {
        dailyService.aggregateAll();
        return ResponseEntity.ok("✅ Агрегация завершена");
    }

    @Operation(summary = "Агрегировать за конкретный день")
    @PostMapping("/aggregate")
    public ResponseEntity<String> aggregate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dailyService.aggregateAndSave(date);
        return ResponseEntity.ok("✅ Агрегировано за " + date);
    }

    @Operation(summary = "Получить данные за день")
    @GetMapping
    public ResponseEntity<List<AsrDailyEntity>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dailyRepository.findByDate(date));
    }

    // DELETE http://localhost:8080/api/asr-daily/all
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll() {
        dailyService.deleteAll();
        return ResponseEntity.ok("✅ Все записи asr_daily удалены!");
    }
}
