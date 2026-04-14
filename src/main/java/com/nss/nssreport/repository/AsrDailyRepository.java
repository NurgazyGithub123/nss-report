package com.nss.nssreport.repository;

import com.nss.nssreport.domain.entity.AsrDailyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsrDailyRepository extends JpaRepository<AsrDailyEntity, Long> {
    List<AsrDailyEntity> findByDate(LocalDate date);
    boolean existsByDateAndObjectInstance(LocalDate date, String objectInstance);
    void deleteByDate(LocalDate date);
}