package com.sparta.settlementservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "videohistory")
@Getter
@Setter
public class VideoViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Videos video;

    //MSA환경 문제로 User 대신 userid로 바꿈
    private Long userId;

    private Long videoViewCount;  // 일반 비디오 조회수
    private Long adViewCount;  // 광고 시청 횟수

    private LocalDateTime lastPlayedDate;
    private LocalDate playedDate;

    private Integer currentPosition;


}
