package com.sparta.settlementservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AdVideos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String adType;

    private Long viewCount;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Videos video;  // 광고가 속한 비디오

}
