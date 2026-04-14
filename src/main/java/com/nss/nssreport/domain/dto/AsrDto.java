package com.nss.nssreport.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AsrDto {

    private String objectInstance;
    private LocalDate date;
    private LocalTime time;
    private Long callAttempt;
    private Long answerTimes;
    private Long abandonBeforeRing;
    private Long abandonAfterRing;
    private Long ringedNoAnswer;
    private Long userDetBusy;
    private Long userBusy;
    private Long invalidAddress;
    private Long pagingNoResponse;
    private Long absentSubscriber;
    private Long serviceRestricted;
    private Long specialSignal;
    private Long calledNoRespond;
    private Double asr;
    private Double ner;
    private Long sucAttempt;
}
