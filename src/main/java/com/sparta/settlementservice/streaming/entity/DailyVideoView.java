package com.sparta.settlementservice.streaming.entity;


import com.sparta.settlementservice.streaming.dto.PlayRequest;
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
        indexes = {
                @Index(name = "idx_video_id", columnList = "videoId"), // 기존 인덱스
        }
)

public class DailyVideoView extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId; // 동영상 정보와 연관 관계

    @Column( nullable = false)
    private LocalDate date; // 날짜별 조회수를 기록하기 위한 날짜

    private Long viewCount = 0L; // 날짜별 조회수 초기값 0으로 설정

    private Long adViewCount = 0L; // 날짜별 광고 시청 횟수 초기값 0으로 설정

    //총합 playtime 시간 계산 때문에 Long 통일
    private Long playTime = 0L;

    private Long currentPosition = 0L;

    private String statType;


    // 생성자
    public DailyVideoView(PlayRequest playRequest,Long videoId) {
        this.videoId = videoId;
        this.viewCount = 1L;
        this.currentPosition= playRequest.getCurrentPosition();
        this.playTime = playRequest.getPlaytime();
        this.adViewCount = playRequest.getPlaytime() /5;
    }

    public DailyVideoView(Long videoId, Long viewCount, Long adViewCount, Long playTime, Long currentPosition) {
        this.videoId = videoId;
        this.viewCount = viewCount;
        this.adViewCount = adViewCount;
        this.playTime = playTime;
        this.currentPosition = currentPosition;
    }


    public void increasePlaytime(Long currentposition) {
        this.playTime += currentposition;
    }

}
