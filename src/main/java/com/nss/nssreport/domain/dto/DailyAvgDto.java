package com.nss.nssreport.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

// DTO для средних показателей за сутки
@Data
@AllArgsConstructor
public class DailyAvgDto {

    private LocalDate date;
    private String objectInstance;
    private Double asr;           // ASR = totalAnswer / totalCall * 100
    private Double ner;           // NER = totalSuc / totalCall * 100
    private Long totalSucAttempt; // Всего успешных доставок за день
    private Long totalCallAttempt;// Всего попыток за день
    private Long totalAnswer;     // Всего отвеченных за день

}
