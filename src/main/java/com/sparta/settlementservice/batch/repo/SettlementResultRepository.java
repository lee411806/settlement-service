package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.SettlementResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementResultRepository extends JpaRepository<SettlementResult, Long> {
}
