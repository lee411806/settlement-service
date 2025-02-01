package com.sparta.settlementservice.streaming.dto;

import lombok.Getter;

@Getter
public class PlayResponseDto {
    int currentPosition;
    boolean abusing;
    long viewCount;
}
