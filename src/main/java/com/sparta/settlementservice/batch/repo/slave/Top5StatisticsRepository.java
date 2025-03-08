package com.sparta.settlementservice.batch.repo.slave;

import com.sparta.settlementservice.batch.entity.Top5Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface Top5StatisticsRepository extends JpaRepository<Top5Statistics, Long> {


    List<Top5Statistics> findTop5ByDateTypeAndStartDateBetweenAndStaticTypeOrderByValueDesc(String dateType, LocalDate start, LocalDate end, String staticType);
}
