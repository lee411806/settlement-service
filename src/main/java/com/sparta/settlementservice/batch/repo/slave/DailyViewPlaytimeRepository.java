package com.sparta.settlementservice.batch.repo.slave;

import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyViewPlaytimeRepository extends JpaRepository<DailyViewPlaytime, Long> {

}
