package com.sparta.settlementservice.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

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
    @Column(name = "createdAt")
    private LocalDate createdAt;       // 날짜
    private Long totalViewCount = 0L;  // 해당 날짜의 조회수 합계
    private Long totalAdViewCount= 0L; // 해당 날짜의 광고 조회수 합계
    private Long totalPlayTime= 0L;   // 해당 날짜의 재생시간 합계

    // 생성자
    public DailyViewPlaytime(Long videoId, Long totalViewCount, Long totalAdViewCount, Long totalPlayTime, LocalDate createdAt ) {
        this.videoId = videoId;
        this.totalViewCount = totalViewCount;
        this.totalAdViewCount = totalAdViewCount;
        this.totalPlayTime = totalPlayTime;
        this.createdAt  = createdAt ;
    }


    public DailyViewPlaytime(long videoId, LocalDate createdAt , long totalViewCount, long totalAdViewCount) {
        this.videoId = videoId;
        this.createdAt  = createdAt ;
        this.totalViewCount = totalViewCount;
        this.totalAdViewCount = totalAdViewCount;
    }

    public DailyViewPlaytime(long videoId, LocalDate createdAt, long totalViewCount, long totalAdViewCount, long totalPlayTime) {
        this.videoId = videoId;
        this.createdAt = createdAt ;
        this.totalViewCount = totalViewCount;
        this.totalAdViewCount = totalAdViewCount;
        this.totalPlayTime = totalPlayTime;
    }
}
