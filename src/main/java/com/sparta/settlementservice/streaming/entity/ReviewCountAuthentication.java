package com.sparta.settlementservice.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ReviewCountAuthentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String jwtToken;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private Integer lastActionTime;

}
