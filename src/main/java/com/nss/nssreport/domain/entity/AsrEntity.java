package com.nss.nssreport.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity для хранения сырых счетчиков NSS.
 * Эти данные загружаются из файла отчета.
 */
@Data
@Entity
@Table(name = "asr_kpi")
public class AsrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_instance")
    private String objectInstance;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "call_attempt")
    private Long callAttempt;

    @Column(name = "answer_times")
    private Long answerTimes;

    @Column(name = "asr")
    private Double asr;

    @Column(name = "ner")
    private Double ner;

    @Column(name = "suc_attempt")
    private Long sucAttempt;

    @Column(name = "abandon_before_ring")
    private Long abandonBeforeRing;

    @Column(name = "abandon_after_ring")
    private Long abandonAfterRing;

    @Column(name = "ringed_no_answer")
    private Long ringedNoAnswer;

    @Column(name = "user_det_busy")
    private Long userDetBusy;

    @Column(name = "user_busy")
    private Long userBusy;

    @Column(name = "invalid_address")
    private Long invalidAddress;

    @Column(name = "paging_no_response")
    private Long pagingNoResponse;

    @Column(name = "absent_subscriber")
    private Long absentSubscriber;

    @Column(name = "service_restricted")
    private Long serviceRestricted;

    @Column(name = "special_signal")
    private Long specialSignal;

    @Column(name = "called_no_respond")
    private Long calledNoRespond;

}
