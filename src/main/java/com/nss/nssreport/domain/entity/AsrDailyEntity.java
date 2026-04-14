package com.nss.nssreport.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "asr_daily")
public class AsrDailyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "object_instance")
    private String objectInstance;

    @Column(name = "asr")
    private Double asr;

    @Column(name = "ner")
    private Double ner;

    @Column(name = "total_call")
    private Long totalCall;

    @Column(name = "total_answer")
    private Long totalAnswer;

    @Column(name = "total_suc")
    private Long totalSuc;
}