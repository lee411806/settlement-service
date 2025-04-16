package com.sparta.settlementservice.settlement.service;


import com.sparta.settlementservice.batch.entity.Top5Statistics;
import com.sparta.settlementservice.batch.repo.slave.Top5StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Top5StatisticsService {

    private final Top5StatisticsRepository top5StatisticsRepository;

    public List<Top5Statistics> getTop5Statistics(String dateType, LocalDate startDate, String staticType) {
        LocalDate start, end;

        if ("DAILY".equalsIgnoreCase(dateType)) {
            start = startDate;
            end = startDate;
        } else if ("WEEKLY".equalsIgnoreCase(dateType)) {
            start = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); // 주의 월요일
            end = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)); // 주의 일요일
        } else if ("MONTHLY".equalsIgnoreCase(dateType)) {
            start = startDate.withDayOfMonth(1); // 월의 첫째 날
            end = startDate.with(TemporalAdjusters.lastDayOfMonth()); // 월의 마지막 날
        } else {
            throw new IllegalArgumentException("Invalid dateType: " + dateType);
        }


        // DB 조회
        return top5StatisticsRepository.findTop5ByDateTypeAndStartDateBetweenAndStaticTypeOrderByValueDesc(
                dateType, start, end, staticType);

    }
}
