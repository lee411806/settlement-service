package com.sparta.settlementservice.streaming.entity;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class VideoAdRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name="video_id")
    private Videos videos;

    @ManyToOne
    @JoinColumn(name="advideo_id")
    private AdVideos adVideos;


    //history table에 넣기
    private Long startTime;  // 광고가 시작되는 시점 (초 단위로 기록)

    private Long user_id;

    private Long revenue;


}
