package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.MonthViewPlaytime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MonthViewPlaytimeRepository extends JpaRepository<MonthViewPlaytime, Long> {
    MonthViewPlaytime findByVideoIdAndStartDateAndEndDate(Long videoId, LocalDate startDate, LocalDate endDate);
}
