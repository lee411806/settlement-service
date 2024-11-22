package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.WeekViewPlaytime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WeekViewPlaytimeRepository extends JpaRepository<WeekViewPlaytime, Long> {
    List<WeekViewPlaytime> findByVideoIdInAndStartDate(List<Long> videoIds, LocalDate with);
}
