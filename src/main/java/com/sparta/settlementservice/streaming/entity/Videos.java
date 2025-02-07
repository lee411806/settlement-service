package com.sparta.settlementservice.streaming.entity;


import com.sparta.settlementservice.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Videos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    @ManyToOne(fetch = FetchType.LAZY)  // ManyToOne 관계 설정
    private UserEntity uploadingUser;  //  Long → UserEntity 객체로 변경

    //광고영상을 넣기 위한 동영상 길이 설정
    @Column(nullable = false)
    private Integer duration;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<AdVideos> ads = new ArrayList<>();  // Video에 포함된 광고 리스트

}
