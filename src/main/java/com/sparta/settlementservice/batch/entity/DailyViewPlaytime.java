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
public class DailyViewPlaytime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;         // 비디오 ID
    private LocalDate date;       // 날짜
    private Long totalViewCount;  // 해당 날짜의 조회수 합계
    private Long totalAdViewCount; // 해당 날짜의 광고 조회수 합계
    private Long totalPlayTime;   // 해당 날짜의 재생시간 합계

    // 생성자
    public DailyViewPlaytime(Long videoId, LocalDate date, Long totalViewCount, Long totalAdViewCount, Long totalPlayTime) {
        this.videoId = videoId;
        this.date = date;
        this.totalViewCount = totalViewCount;
        this.totalAdViewCount = totalAdViewCount;
        this.totalPlayTime = totalPlayTime;
    }
}
