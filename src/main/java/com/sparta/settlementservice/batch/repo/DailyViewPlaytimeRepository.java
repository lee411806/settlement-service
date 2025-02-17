package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyViewPlaytimeRepository extends JpaRepository<DailyViewPlaytime, Long> {

    List<DailyViewPlaytime> findByVideoIdAndCreatedAt(Long videoId, LocalDate date);

    List<DailyViewPlaytime> findByVideoId(Long videoId);

    boolean existsByVideoIdAndCreatedAt(Long videoId, LocalDate date);
}
