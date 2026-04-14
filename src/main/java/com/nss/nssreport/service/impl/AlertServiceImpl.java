package com.nss.nssreport.service.impl;

import com.nss.nssreport.domain.dto.AlertDto;
import com.nss.nssreport.repository.AsrRepository;
import com.nss.nssreport.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements  AlertService{

    private final AsrRepository repository;

    // Порог падения — 20%
    private static final double DROP_THRESHOLD = 20.0;

    @Override
    public List<AlertDto> findAsrDropAlerts() {

        List<Object[]> rows = repository.findAsrDropData();

        return rows.stream()
                .map(row -> {
                    String route = (String) row[0];

                    LocalDate lastDate = row[1] instanceof LocalDate
                            ? (LocalDate) row[1]
                            : ((java.sql.Date) row[1]).toLocalDate();

                    double lastAsr = ((Number) row[2]).doubleValue();
                    double avgAsr  = ((Number) row[3]).doubleValue();
                    double dropAsr = avgAsr - lastAsr;

                    double lastNer = ((Number) row[4]).doubleValue();
                    double avgNer  = ((Number) row[5]).doubleValue();
                    double dropNer = avgNer - lastNer;

                    // 🔴 ALERT — оба упали на 20%
                    // 🟡 WARN  — только ASR упал (мало звонков)
                    // 🟢 OK    — всё в норме
                    String status;
                    if (dropAsr >= DROP_THRESHOLD && dropNer >= DROP_THRESHOLD) {
                        status = "🔴 ALERT";
                    } else if (dropAsr >= DROP_THRESHOLD) {
                        status = "🟡 WARN";
                    } else {
                        status = "🟢 OK";
                    }

                    return new AlertDto(
                            route, lastDate,
                            lastAsr, avgAsr, dropAsr,
                            lastNer, avgNer, dropNer,
                            status
                    );
                })
                .collect(Collectors.toList());
    }
}


