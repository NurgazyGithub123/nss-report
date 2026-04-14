package com.nss.nssreport.controller;


import com.nss.nssreport.domain.dto.AlertDto;
import com.nss.nssreport.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alerts", description = "Мониторинг падения ASR и NER")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // GET http://localhost:8080/api/alerts/asr-drop
    // Возвращает маршруты где ASR резко упал
    @Operation(summary = "Все маршруты с анализом падения", description = "Сравнивает последний день с предыдущими 3 днями")
    @GetMapping("/asr-drop")
    public ResponseEntity<List<AlertDto>> getAsrDropAlerts() {
        return ResponseEntity.ok(alertService.findAsrDropAlerts());
    }

    // GET http://localhost:8080/api/alerts/asr-drop?onlyAlerts=true
    // Возвращает только маршруты с алертом
    @Operation(summary = "Только алерты 🔴", description = "Маршруты где ASR и NER упали более чем на 20%")
    @GetMapping("/asr-drop/only")
    public ResponseEntity<List<AlertDto>> getOnlyAlerts() {
        List<AlertDto> all = alertService.findAsrDropAlerts();
        List<AlertDto> onlyAlerts = all.stream()
                .filter(a -> a.getStatus().contains("ALERT"))
                .toList();
        return ResponseEntity.ok(onlyAlerts);
    }
}
