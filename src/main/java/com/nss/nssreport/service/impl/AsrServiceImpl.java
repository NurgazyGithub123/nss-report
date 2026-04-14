package com.nss.nssreport.service.impl;

import com.nss.nssreport.domain.dto.AlertDto;
import com.nss.nssreport.domain.dto.AsrDto;
import com.nss.nssreport.domain.dto.DailyAvgDto;
import com.nss.nssreport.domain.entity.AsrEntity;
import com.nss.nssreport.domain.mapper.AsrMapper;
import com.nss.nssreport.repository.AsrRepository;
import com.nss.nssreport.service.AsrService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с сырыми данными ASR.
 */
@Service
@RequiredArgsConstructor  // Lombok: генерирует конструктор для final полей (инжекция зависимостей)
public class AsrServiceImpl implements AsrService {

    private final AsrRepository repository;
    private final AsrMapper mapper;

    @Override
    public void saveAll(List<AsrDto> dtos) {
        List<AsrEntity> entities = dtos.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }

    @Override
    public List<AsrDto> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsrDto> getByDate(LocalDate date) {
        return repository.findByDate(date).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsrDto> getByObjectInstance(String objectInstance) {
        return repository.findByObjectInstance(objectInstance).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsrDto> getByDateAndObjectInstance(LocalDate date, String objectInstance) {
        return repository.findByDateAndObjectInstance(date, objectInstance).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsrDto> getByDateBetween(LocalDate from, LocalDate to) {
        return repository.findByDateBetween(from, to).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsrDto> getByDateBetweenAndObjectInstance(
            LocalDate from, LocalDate to, String objectInstance) {
        return repository.findByDateBetweenAndObjectInstance(from, to, objectInstance).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DailyAvgDto> getDailyAvgByDate(LocalDate date) {
        return repository.findDailyAvgByDate(date)
                .stream()
                .map(row -> new DailyAvgDto(
                        // Hibernate возвращает LocalDate напрямую — не нужен cast к sql.Date
                        row[0] instanceof LocalDate
                                ? (LocalDate) row[0]
                                : ((java.sql.Date) row[0]).toLocalDate(),
                        (String)  row[1],
                        (Double)  row[2],   // ASR
                        (Double)  row[3],   // NER
                        ((Number) row[4]).longValue(),  // totalSuc
                        ((Number) row[5]).longValue(),  // totalCall
                        ((Number) row[6]).longValue()   // totalAnswer
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DailyAvgDto> getDailyAvgByDateBetween(LocalDate from, LocalDate to) {
        return repository.findDailyAvgByDateBetween(from, to)
                .stream()
                .map(row -> new DailyAvgDto(
                        row[0] instanceof LocalDate
                                ? (LocalDate) row[0]
                                : ((java.sql.Date) row[0]).toLocalDate(),
                        (String)  row[1],
                        (Double)  row[2],
                        (Double)  row[3],
                        ((Number) row[4]).longValue(),
                        ((Number) row[5]).longValue(),
                        ((Number) row[6]).longValue()
                ))
                .collect(Collectors.toList());
    }

}