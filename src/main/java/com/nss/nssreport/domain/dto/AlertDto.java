package com.nss.nssreport.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AlertDto {

    private String objectInstance;  // Маршрут
    private LocalDate lastDate;     // Последний день

    private Double lastAsr;         // ASR последнего дня
    private Double avgAsr;          // Средний ASR за 3 дня
    private Double dropAsr;         // Падение ASR

    private Double lastNer;         // NER последнего дня
    private Double avgNer;          // Средний NER за 3 дня
    private Double dropNer;         // Падение NER

    private String status;          // 🔴 ALERT / 🟡 WARN / 🟢 OK

}
