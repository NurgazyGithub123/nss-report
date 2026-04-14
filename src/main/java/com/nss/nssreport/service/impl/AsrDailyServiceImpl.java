package com.nss.nssreport.service.impl;

import com.nss.nssreport.domain.entity.AsrDailyEntity;
import com.nss.nssreport.repository.AsrDailyRepository;
import com.nss.nssreport.repository.AsrRepository;
import com.nss.nssreport.service.AsrDailyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsrDailyServiceImpl implements AsrDailyService {

    private final AsrRepository asrRepository;
    private final AsrDailyRepository dailyRepository;

    @Override
    public void aggregateAndSave(LocalDate date) {
        // Удаляем старые данные за этот день если есть
        dailyRepository.deleteByDate(date);

        List<Object[]> rows = asrRepository.findDailyAvgByDate(date);

        for (Object[] row : rows) {
            AsrDailyEntity entity = new AsrDailyEntity();

            entity.setDate(row[0] instanceof LocalDate
                    ? (LocalDate) row[0]
                    : ((java.sql.Date) row[0]).toLocalDate());
            entity.setObjectInstance((String) row[1]);
            entity.setAsr((Double) row[2]);
            entity.setNer((Double) row[3]);
            entity.setTotalSuc(((Number) row[4]).longValue());
            entity.setTotalCall(((Number) row[5]).longValue());
            entity.setTotalAnswer(((Number) row[6]).longValue());

            dailyRepository.save(entity);
        }

        System.out.println("Агрегировано за " + date + ": " + rows.size() + " маршрутов");
    }

    @Override
    public void aggregateAll() {
        // Берём все уникальные даты из asr_kpi и агрегируем каждую
        asrRepository.findAll().stream()
                .map(e -> e.getDate())
                .filter(d -> d != null)
                .distinct()
                .sorted()
                .forEach(this::aggregateAndSave);
    }
}