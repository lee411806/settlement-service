package com.sparta.settlementservice.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class MonthViewPlaytime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;          // 비디오 ID
    private LocalDate startDate;    // 월간 시작 날짜
    private LocalDate endDate;      // 월간 종료 날짜
    private Long totalViewCount;    // 해당 월간 조회수 합계
    private Long totalAdViewCount;  // 해당 월간 광고 조회수 합계
    private Long totalPlayTime;     // 해당 월간 재생시간 합계

    public MonthViewPlaytime(Long videoId, LocalDate startDate, LocalDate endDate, Long totalViewCount, Long totalPlayTime) {
        this.videoId = videoId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalViewCount = totalViewCount;
        this.totalPlayTime = totalPlayTime;
    }

    public MonthViewPlaytime(Long videoId, LocalDate localDate, LocalDate localDate1, long l, long l1, long l2) {
        this.videoId = videoId;
        this.startDate = localDate;
        this.endDate = localDate1;
        this.totalViewCount = l;
        this.totalPlayTime = l1;
        this.totalAdViewCount = l;
    }
}