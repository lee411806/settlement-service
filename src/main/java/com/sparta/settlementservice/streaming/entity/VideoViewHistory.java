package com.sparta.settlementservice.streaming.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class VideoViewHistory extends UpdatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;

    //MSA환경 문제로 User 대신 userid로 바꿈
    private Long userId;

    private Long currentPosition;


    public VideoViewHistory(Long userId, Long videoId, Long currentPosition) {
        this.userId = userId;
        this.videoId = videoId;
        this.currentPosition = currentPosition;
    }
}
