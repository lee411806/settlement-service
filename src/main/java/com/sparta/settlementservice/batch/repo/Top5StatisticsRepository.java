package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.Top5Statistics;
import org.springframework.data.jpa.repository.JpaRepository;


public interface Top5StatisticsRepository extends JpaRepository<Top5Statistics, Long> {
}
