package com.sparta.settlementservice.batch.schedule;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class StatisticsSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public StatisticsSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    // Daily Static 배치 처리
//    @Scheduled(initialDelay = 4000, fixedRate = Long.MAX_VALUE) // 스케줄러 설정
//    public void runDailyStatisticsJob() {
//        long startTime = System.currentTimeMillis(); // 시작 시간 기록
//
//        try {
//            Job job = jobRegistry.getJob("dailyStatisticsJob");
//            // 현재 시간을 기반으로 jobParameters 설정 (매 실행 시 고유한 파라미터 생성)
//            jobLauncher.run(job, new JobParametersBuilder()
//                    .addString("runDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)) // 실행 날짜 추가
//                    .addLong("timestamp", System.currentTimeMillis()) // 매번 고유 실행을 보장하는 timestamp
//                    .toJobParameters());
//
//        } catch (JobExecutionAlreadyRunningException | JobRestartException e) {
//            System.err.println("Job이 이미 실행 중이거나 재시작할 수 없습니다: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Job 실행 중 예외 발생: " + e.getMessage());
//        }
//
//        long endTime = System.currentTimeMillis(); // 종료 시간 기록
//        long executionTime = endTime - startTime; // 실행 시간 계산
//
//        System.out.println("Daily Statistics Job Execution Time: " + executionTime + " ms");
//    }


    // ✅ Spring Boot 시작 후 5초 뒤에 한 번만 실행
    @Scheduled(initialDelay = 4000, fixedRate = Long.MAX_VALUE)
    public void runDailyTop5StatisticsJob() {
        long startTime = System.currentTimeMillis(); // ✅ 시작 시간 기록

        try {
            Job job = jobRegistry.getJob("top5StatisticsBatchJob");  // ✅ Job 이름 변경 (올바른 Job 실행)

            // ✅ 현재 시간을 기반으로 jobParameters 설정 (중복 실행 방지)
            jobLauncher.run(job, new JobParametersBuilder()
                    .addString("runDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)) // 실행 날짜 추가
                    .addLong("timestamp", System.currentTimeMillis()) // ✅ 매번 고유 실행을 보장하는 timestamp
                    .toJobParameters());

        } catch (NoSuchJobException e) {
            System.err.println("등록된 Job을 찾을 수 없습니다: " + e.getMessage());
        } catch (JobExecutionAlreadyRunningException | JobRestartException e) {
            System.err.println("Job이 이미 실행 중이거나 재시작할 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Job 실행 중 예외 발생: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis(); // ✅ 종료 시간 기록
        long executionTime = endTime - startTime; // ✅ 실행 시간 계산

        System.out.println("Daily Statistics Job Execution Time: " + executionTime + " ms");
    }

}