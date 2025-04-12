package com.sparta.settlementservice.settlement.service;

import com.sparta.settlementservice.batch.entity.SettlementResult;
import com.sparta.settlementservice.batch.repo.slave.SettlementResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementResultRepository settlementResultRepository; //  변경됨

    public List<SettlementResult> getSettlement(Long videoId, String dateType, LocalDate startDate) { // Settlement → SettlementResult
        LocalDate start, end;
        System.out.println(dateType);
        System.out.println(startDate);
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

        return settlementResultRepository.findByVideoIdAndDateTypeAndStartDateBetween(
                videoId, dateType, start, end
        );
    }
}

