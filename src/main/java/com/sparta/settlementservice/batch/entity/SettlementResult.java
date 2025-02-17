package com.sparta.settlementservice.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SettlementResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long videoId;      //  비디오 ID
    private Long videoRevenue; //  영상 정산 금액
    private Long adRevenue;    //  광고 정산 금액
    private Long totalRevenue; //  총 정산 금액 (영상 + 광고)
    private LocalDate startDate; //  정산 시작 날짜
    private LocalDate endDate;   //  정산 종료 날짜
    private String dateType;   //  정산 기간 (DAILY, WEEKLY, MONTHLY)
}
