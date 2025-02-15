package com.sparta.settlementservice.batch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.jmx.support.MetricType;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Top5Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date; //  기준 날짜 (해당 일/주/월의 마지막 날짜)

    private String dateType; //  통계 유형 (DAILY, WEEKLY, MONTHLY)

    private String staticType; //  기준 (VIEW_COUNT, PLAY_TIME)

    private Long videoId; //  비디오 ID

    private int ranking;

    private Long value; // 해당 기준의 수치 (조회수 or 재생 시간)


}
