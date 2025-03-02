package com.sparta.settlementservice.streaming.repository;


import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyVideoViewRepository extends JpaRepository<DailyVideoView, Long> {

//    // 특정 동영상과 날짜(today)에 해당하는 DailyVideoView 데이터를 조회 (Optional 반환)
//    Optional<DailyVideoView> findByVideoIdAndDate(Long videoId, LocalDate today);
//
//    // 주어진 날짜 범위(startDate ~ endDate) 내에서 조회수가 높은 상위 5개의 DailyVideoView 데이터를 조회 (내림차순 정렬)
//    List<DailyVideoView> findTop5ByDateBetweenOrderByViewCountDesc(LocalDate startDate, LocalDate endDate);
//
//
//
//    List<DailyVideoView> findTop5ByDateBetweenOrderByPlayTimeDesc(LocalDate startDate, LocalDate endDate);
//
//    List<DailyVideoView> findByDateBetween(LocalDate startDate, LocalDate endDate);


//    @Query("SELECT d FROM DailyVideoView d WHERE d.createdAt = :date")
//    List<DailyVideoView> findByDate(@Param("date") LocalDate date);


}
