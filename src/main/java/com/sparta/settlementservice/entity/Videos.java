package com.sparta.settlementservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name="videos")
@Getter
@Setter
public class Videos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    //외래키 설정 필요..?
    @Column(nullable = false)
    private Long uploadingUser;

    @Column(nullable = false)
    private Long viewCount;

    @Column(nullable = false)
    private Integer lastPlayedTime;

    //광고영상을 넣기 위한 동영상 길이 설정
    @Column(nullable = false)
    private Integer duration;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<AdVideos> ads = new ArrayList<>();  // Video에 포함된 광고 리스트

}
