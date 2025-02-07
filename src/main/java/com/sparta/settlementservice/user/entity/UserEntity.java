package com.sparta.settlementservice.user.entity;

import com.sparta.settlementservice.streaming.entity.Videos;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "uploadingUser", cascade = CascadeType.ALL) //  연관관계 설정
    private List<Videos> uploadedVideos = new ArrayList<>();

    private String username;
    private String name;

    private String email;

    private String role;
}
