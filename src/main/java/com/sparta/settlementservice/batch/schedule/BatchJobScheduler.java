package com.sparta.settlementservice.batch.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final Job dailyStatisticsJob;
    private final Job top5StatisticsBatchJob;
    private final Job settlementResultBatchJob;

    @Scheduled(initialDelay = 4000, fixedRate = Long.MAX_VALUE)
    public void runBatchJobs() {
        try {
            System.out.println("[Batch Scheduler] 정산 배치 시작");

            // Daily Statistics Job 실행
            JobExecution dailyExecution = jobLauncher.run(dailyStatisticsJob, createUniqueJobParameters());

            if (dailyExecution.getStatus() == BatchStatus.COMPLETED) {
                System.out.println("dailyStatisticsJob 완료. top5StatisticsBatchJob 실행 시작...");

                // TOP 5 Statistics Job 실행
                JobExecution top5Execution = jobLauncher.run(top5StatisticsBatchJob, createUniqueJobParameters());

                if (top5Execution.getStatus() == BatchStatus.COMPLETED) {
                    System.out.println("top5StatisticsBatchJob 완료. settlementResultBatchJob 실행 시작...");

                    // Settlement Result Batch Job 실행
                    JobExecution settlementExecution = jobLauncher.run(settlementResultBatchJob, createUniqueJobParameters());

                    if (settlementExecution.getStatus() == BatchStatus.COMPLETED) {
                        System.out.println("모든 정산 배치 완료!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("배치 실행 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * Job 실행 시마다 새로운 JobParameters 생성 (timestamp 적용)
     */
    private JobParameters createUniqueJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // 실행할 때마다 새로운 값 추가
                .toJobParameters();
    }


//    @Scheduled(initialDelay = 4000, fixedRate = Long.MAX_VALUE)// 매일 새벽 1시 실행
//    public void runTop5StatisticsBatch() {
//        try {
//            System.out.println("[Batch Scheduler] Top5 통계 배치 실행 시작 - " + LocalDateTime.now());
//
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("time", System.currentTimeMillis()) // 중복 실행 방지용 파라미터
//                    .toJobParameters();
//
//            jobLauncher.run(top5StatisticsBatchJob, jobParameters);
//
//            System.out.println("[Batch Scheduler] Top5 통계 배치 실행 완료 - " + LocalDateTime.now());
//        } catch (Exception e) {
//            System.err.println("[Batch Scheduler] Top5 통계 배치 실행 중 오류 발생: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

}
