package com.sparta.settlementservice.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementStats {
    private Long videoId;         //  비디오 ID
    private Long totalViewCount;  //  해당 기간 동안 총 조회수
    private Long totalAdViewCount; // 해당 기간 동안 총 광고 조회수
    private String dateType;      //  정산 타입 (DAILY, WEEKLY, MONTHLY)
    private LocalDate startDate;  //  정산 시작 날짜
    private LocalDate endDate;    //  정산 종료 날짜
}

