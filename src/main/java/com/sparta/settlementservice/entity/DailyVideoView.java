package com.sparta.settlementservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "daily_video_view",
        indexes = {
                @Index(name = "idx_video_id", columnList = "videoId"), // 기존 인덱스
        }
)

public class DailyVideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId; // 동영상 정보와 연관 관계

    @Column(name = "date", nullable = false)
    private LocalDate date; // 날짜별 조회수를 기록하기 위한 날짜

    @Column(name = "view_count")
    private Long viewCount = 0L; // 날짜별 조회수 초기값 0으로 설정

    @Column(name = "adview_count")
    private Long adViewCount = 0L; // 날짜별 광고 시청 횟수 초기값 0으로 설정

    @Column(name="playtime")
    private Long playTime = 0L;

    private String statType;

    public DailyVideoView(Long videoId, LocalDate today) {
        this.videoId = videoId;
        this.date = today;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount += 1;
    }


    public void incrementAdViewCount() {
        this.adViewCount += 1;
    }

    public void increasePlaytime(int currentposition) {
        this.playTime += currentposition;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }
}
