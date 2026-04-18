package com.nss.nssreport.repository;


import com.nss.nssreport.domain.entity.AsrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;



@Repository
public interface AsrRepository extends JpaRepository<AsrEntity, Long>{

    // Фильтр по дате
    List<AsrEntity> findByDate(LocalDate date);

    // Фильтр по маршруту
    List<AsrEntity> findByObjectInstance(String objectInstance);

    // Фильтр по дате И маршруту
    List<AsrEntity> findByDateAndObjectInstance(LocalDate date, String objectInstance);

    // Фильтр по диапазону дат
    List<AsrEntity> findByDateBetween(LocalDate from, LocalDate to);

    // Фильтр по диапазону дат И маршруту
    List<AsrEntity> findByDateBetweenAndObjectInstance(
            LocalDate from, LocalDate to, String objectInstance);

    // Удалить по дате
    void deleteByDate(LocalDate date);

    // Удалить по маршруту
    void deleteByObjectInstance(String objectInstance);

    // Удалить по дате и маршруту
    void deleteByDateAndObjectInstance(LocalDate date, String objectInstance);

    // Агрегация по дате
    @Query("SELECT a.date, a.objectInstance, " +
            "CASE WHEN SUM(a.callAttempt) > 0 THEN SUM(a.answerTimes) * 100.0 / SUM(a.callAttempt) ELSE 0 END, " +
            "CASE WHEN SUM(a.callAttempt) > 0 THEN SUM(a.sucAttempt) * 100.0 / SUM(a.callAttempt) ELSE 0 END, " +
            "SUM(a.sucAttempt), SUM(a.callAttempt), SUM(a.answerTimes) " +
            "FROM AsrEntity a WHERE a.date = :date " +
            "GROUP BY a.date, a.objectInstance ORDER BY a.objectInstance")
    List<Object[]> findDailyAvgByDate(@Param("date") LocalDate date);

    @Query("SELECT a.date, a.objectInstance, " +
            "CASE WHEN SUM(a.callAttempt) > 0 THEN SUM(a.answerTimes) * 100.0 / SUM(a.callAttempt) ELSE 0 END, " +
            "CASE WHEN SUM(a.callAttempt) > 0 THEN SUM(a.sucAttempt) * 100.0 / SUM(a.callAttempt) ELSE 0 END, " +
            "SUM(a.sucAttempt), SUM(a.callAttempt), SUM(a.answerTimes) " +
            "FROM AsrEntity a WHERE a.date BETWEEN :from AND :to " +
            "GROUP BY a.date, a.objectInstance ORDER BY a.date, a.objectInstance")
    List<Object[]> findDailyAvgByDateBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ✅ Новый единый запрос для алертов
    @Query(value = """
    SELECT
        last_day.object_instance,
        last_day.last_date,
        last_day.last_asr,
        prev.avg_asr,
        last_day.last_ner,
        prev.avg_ner
    FROM (
        SELECT
            object_instance,
            MAX(date) as last_date,
            CASE WHEN SUM(call_attempt) > 0
                 THEN SUM(answer_times) * 100.0 / SUM(call_attempt)
                 ELSE 0 END as last_asr,
            CASE WHEN SUM(call_attempt) > 0
                 THEN SUM(suc_attempt) * 100.0 / SUM(call_attempt)
                 ELSE 0 END as last_ner
        FROM asr_kpi
        WHERE call_attempt > 0
        GROUP BY object_instance
    ) last_day
    JOIN (
        SELECT
            a.object_instance,
            CASE WHEN SUM(a.call_attempt) > 0
                 THEN SUM(a.answer_times) * 100.0 / SUM(a.call_attempt)
                 ELSE 0 END as avg_asr,
            CASE WHEN SUM(a.call_attempt) > 0
                 THEN SUM(a.suc_attempt) * 100.0 / SUM(a.call_attempt)
                 ELSE 0 END as avg_ner
        FROM asr_kpi a
        JOIN (
            SELECT object_instance, MAX(date) as last_date
            FROM asr_kpi
            GROUP BY object_instance
        ) ld ON a.object_instance = ld.object_instance
        WHERE a.date < ld.last_date
          AND a.date >= ld.last_date - INTERVAL '3 days'
          AND a.call_attempt > 0
        GROUP BY a.object_instance
    ) prev ON last_day.object_instance = prev.object_instance
    ORDER BY (prev.avg_asr - last_day.last_asr) DESC
    """, nativeQuery = true)
    List<Object[]> findAsrDropData();


}