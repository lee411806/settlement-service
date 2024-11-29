package com.sparta.settlementservice.batch.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class TimedJobExecutionListener implements JobExecutionListener {

    private long startTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = System.currentTimeMillis();
        System.out.println("[Job] Starting job...");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        double durationSeconds = durationMillis / 1000.0;

        // 총 처리량 (Step 처리량 합산)
        int totalProcessed = jobExecution.getStepExecutions()
                .stream()
                .mapToInt(step -> (int) step.getReadCount()) // long을 int로 변환
                .sum();

        // 초당 처리량 계산
        double throughput = totalProcessed / durationSeconds;

        System.out.println("[Job] Job finished.");
        System.out.println("[Job] Total Duration: " + durationSeconds + " seconds");
        System.out.println("[Job] Total Processed Items: " + totalProcessed);
        System.out.println("[Job] Throughput: " + throughput + " items/second");

        // 30초 초과 시 경고 출력
        if (durationMillis >= 30000) {
            System.out.println("[Job] Execution timed out after 30 seconds.");
        }
    }
}