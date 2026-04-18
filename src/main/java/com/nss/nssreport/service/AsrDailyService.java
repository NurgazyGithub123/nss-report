package com.nss.nssreport.service;

import java.time.LocalDate;

public interface AsrDailyService {
    void aggregateAndSave(LocalDate date);
    void aggregateAll();
    void deleteAll();
}