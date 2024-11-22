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
@Getter
@Setter
@NoArgsConstructor
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long videoId; // 통계와 관련된 동영상 정보

    private String statType; // 통계 타입: 조회수("view_count") 또는 재생 시간("play_time")

    private String periodType; // 통계 기간: 1일, 1주일, 1달 ("day", "week", "month")

    private LocalDate startDate; // 통계 기간 시작일

    private LocalDate endDate; // 통계 기간 종료일

    private Long viewCount = 0L; // 조회수 합계

    private Long playTime = 0L; // 재생 시간 합계
}