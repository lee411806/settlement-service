package com.sparta.settlementservice.settlement.controller;

import com.sparta.settlementservice.batch.entity.SettlementResult;
import com.sparta.settlementservice.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/{dateType}/{date}")
    public ResponseEntity<List<SettlementResult>> getSettlement(
            @PathVariable String dateType,
            @PathVariable String date) {

        LocalDate startDate = LocalDate.parse(date);
        List<SettlementResult> settlementList = settlementService.getSettlement(dateType, startDate);

        return ResponseEntity.ok(settlementList);
    }
}

