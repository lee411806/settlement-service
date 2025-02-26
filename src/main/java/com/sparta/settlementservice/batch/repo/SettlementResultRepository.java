package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.SettlementResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SettlementResultRepository extends JpaRepository<SettlementResult, Long> {
    List<SettlementResult> findByDateTypeAndStartDateBetween(String dateType, LocalDate start, LocalDate end);
}
