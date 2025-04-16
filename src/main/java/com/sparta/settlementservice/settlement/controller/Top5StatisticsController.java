package com.sparta.settlementservice.settlement.controller;

import com.sparta.settlementservice.batch.entity.Top5Statistics;
import com.sparta.settlementservice.settlement.service.Top5StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/top5")
@RequiredArgsConstructor
public class Top5StatisticsController {

    private final Top5StatisticsService top5StatisticsService;

    @GetMapping("/{dateType}/{date}/{staticType}")
    public ResponseEntity<List<Top5Statistics>> getTop5Statistics(
            @PathVariable String dateType,
            @PathVariable String date,
            @PathVariable String staticType) {


        LocalDate today = LocalDate.parse(date); // String → LocalDate 변환
        List<Top5Statistics> top5 = top5StatisticsService.getTop5Statistics(dateType, today, staticType);
        System.out.println("조회된 Top5 개수: " + top5.size());

        return ResponseEntity.ok(top5);
    }
}
