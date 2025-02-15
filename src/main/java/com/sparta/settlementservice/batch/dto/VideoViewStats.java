package com.sparta.settlementservice.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoViewStats {
    private Long videoId;      // 비디오 ID
    private Long totalValue;   // 조회수 or 재생 시간 (statType에 따라 다름)
    private String statType;   // "VIEW_COUNT" 또는 "PLAY_TIME"
}

