package com.nss.nssreport.service;

import com.nss.nssreport.domain.dto.AsrDto;
import com.nss.nssreport.domain.dto.DailyAvgDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервис для работы с сырыми данными ASR.
 */
public interface AsrService {

    void saveAll(List<AsrDto> dtos);

    // Получить все записи
    List<AsrDto> getAll();

    // Фильтр по дате
    List<AsrDto> getByDate(LocalDate date);

    // Фильтр по маршруту
    List<AsrDto> getByObjectInstance(String objectInstance);

    // Фильтр по дате И маршруту
    List<AsrDto> getByDateAndObjectInstance(LocalDate date, String objectInstance);

    // Фильтр по диапазону дат
    List<AsrDto> getByDateBetween(LocalDate from, LocalDate to);

    // Фильтр по диапазону дат И маршруту
    List<AsrDto> getByDateBetweenAndObjectInstance(
            LocalDate from, LocalDate to, String objectInstance);

    // Средние показатели за конкретную дату
    List<DailyAvgDto> getDailyAvgByDate(LocalDate date);

    // Средние показатели за диапазон дат
    List<DailyAvgDto> getDailyAvgByDateBetween(LocalDate from, LocalDate to);
}
