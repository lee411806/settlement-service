package com.sparta.settlementservice.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job dailyStatisticsJob;
    private final Job top5StatisticsBatchJob;
    private final Job settlementResultBatchJob;

    @GetMapping("/run")
    public ResponseEntity<String> runBatchManually() {
        try {
            System.out.println("[Batch API] 수동 배치 시작");

            JobExecution dailyExecution = jobLauncher.run(dailyStatisticsJob, createUniqueJobParameters());

            if (dailyExecution.getStatus() == BatchStatus.COMPLETED) {
                System.out.println("dailyStatisticsJob 완료 → top5StatisticsBatchJob 실행");
                JobExecution top5Execution = jobLauncher.run(top5StatisticsBatchJob, createUniqueJobParameters());

                if (top5Execution.getStatus() == BatchStatus.COMPLETED) {
                    System.out.println("top5StatisticsBatchJob 완료 → settlementResultBatchJob 실행");
                    JobExecution settlementExecution = jobLauncher.run(settlementResultBatchJob, createUniqueJobParameters());

                    if (settlementExecution.getStatus() == BatchStatus.COMPLETED) {
                        return ResponseEntity.ok("모든 배치 성공적으로 완료!");
                    }
                }
            }
            return ResponseEntity.status(500).body("배치 중 일부 실패!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("배치 실행 중 오류 발생: " + e.getMessage());
        }
    }

    private JobParameters createUniqueJobParameters() {
        return new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
    }
}