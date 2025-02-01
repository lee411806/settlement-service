package com.sparta.settlementservice.streaming.repository;


import com.sparta.settlementservice.streaming.entity.VideoViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoViewHistoryRepository extends JpaRepository<VideoViewHistory, Long> {
    Optional<VideoViewHistory> findByUserIdAndVideoId(Long userId, Long videoId);


//    // 1일 동안 조회수가 높은 동영상 TOP 5
//    @Query(value = "SELECT video_id, SUM(view_count) AS totalViews " +
//            "FROM video_statistics " +
//            "WHERE view_date = CURDATE() " +
//            "GROUP BY video_id " +
//            "ORDER BY totalViews DESC " +
//            "LIMIT 5", nativeQuery = true)
//    List<Object[]> findTop5VideosByViewsToday();
//
//    // 1주 동안 조회수가 높은 동영상 TOP 5
//    @Query(value = "SELECT video_id, SUM(view_count) AS totalViews " +
//            "FROM video_statistics " +
//            "WHERE view_date BETWEEN DATE_SUB(CURDATE(), INTERVAL (WEEKDAY(CURDATE())) DAY) " +
//            "AND DATE_ADD(DATE_SUB(CURDATE(), INTERVAL (WEEKDAY(CURDATE())) DAY), INTERVAL 6 DAY) " +
//            "GROUP BY video_id " +
//            "ORDER BY totalViews DESC " +
//            "LIMIT 5", nativeQuery = true)
//    List<Object[]> findTop5VideosByViewsThisWeek();
//
//    // 1달 동안 조회수가 높은 동영상 TOP 5
//    @Query(value = "SELECT video_id, SUM(view_count) AS totalViews " +
//            "FROM video_statistics " +
//            "WHERE view_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
//            "AND LAST_DAY(CURDATE()) " +
//            "GROUP BY video_id " +
//            "ORDER BY totalViews DESC " +
//            "LIMIT 5", nativeQuery = true)
//    List<Object[]> findTop5VideosByViewsThisMonth();
//
//
//
//    // 하루 조회수를 가져오는 메서드
//    @Query("SELECT SUM(v.videoViewCount) FROM VideoViewHistory v WHERE v.playedDate = :date")
//    Long findDailyViewCount(@Param("date") LocalDate date);
//
//    // 일주일 조회수를 가져오는 메서드 (오늘 포함해서 7일간)
//    @Query("SELECT SUM(v.videoViewCount) FROM VideoViewHistory v WHERE v.playedDate BETWEEN :startOfWeek AND :endOfWeek")
//    Long findWeeklyViewCount(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);
//
//    // 한 달 조회수를 가져오는 메서드 (오늘 포함해서 30일간)
//    @Query("SELECT SUM(v.videoViewCount) FROM VideoViewHistory v WHERE v.playedDate BETWEEN :startOfMonth AND :endOfMonth")
//    Long findMonthlyViewCount(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);
//
//
//    // 하루 광고 조회수를 가져오는 메서드
//    @Query("SELECT SUM(v.adViewCount) FROM VideoViewHistory v WHERE v.playedDate = :date")
//    Long findDailyAdViewCount(@Param("date") LocalDate date);
//
//    // 일주일 광고 조회수를 가져오는 메서드 (오늘 포함해서 7일간)
//    @Query("SELECT SUM(v.adViewCount) FROM VideoViewHistory v WHERE v.playedDate BETWEEN :startOfWeek AND :endOfWeek")
//    Long findWeeklyAdViewCount(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);
//
//    // 한 달 광고 조회수를 가져오는 메서드 (오늘 포함해서 30일간)
//    @Query("SELECT SUM(v.adViewCount) FROM VideoViewHistory v WHERE v.playedDate BETWEEN :startOfMonth AND :endOfMonth")
//    Long findMonthlyAdViewCount(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);



}
