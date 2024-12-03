package com.sparta.settlementservice.batch.schedule;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
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
//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE) // 스케줄러 설정
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


    // 매번 애플리케이션 시작 후 7초 뒤에 한 번 실행
//    @Scheduled(initialDelay = 7000, fixedRate = Long.MAX_VALUE)
//    public void runDailyStatisticsJob() throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("period", "day") // 1일 통계
//                .addLong("time", System.currentTimeMillis()) // 유니크한 파라미터 추가
//                .toJobParameters();
//        jobLauncher.run(jobRegistry.getJob("statisticsJob"), jobParameters);
//    }

    // 매일 자정에 1일 통계 처리




    // 1주 week static 배치처리
//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//    public void runWeeklyStatisticsJob() {
//        try {
//            Job job = jobRegistry.getJob("weeklyStatisticsJob");
//
//            // 현재 시간을 기반으로 jobParameters 설정 (매 실행 시 고유한 파라미터 생성)
//            jobLauncher.run(job, new JobParametersBuilder()
//                    .addString("startDate", LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_DATE))
//                    .addString("endDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
//                    .addLong("timestamp", System.currentTimeMillis()) // 매번 다른 timestamp로 고유 실행
//                    .toJobParameters());
//
//        } catch (JobExecutionAlreadyRunningException | JobRestartException e) {
//            System.err.println("Job이 이미 실행 중이거나 재시작할 수 없습니다: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Job 실행 중 예외 발생: " + e.getMessage());
//        }
//    }




  // week to5 통계 처리
//  @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//  public void runWeeklyTop5StatisticsJob() {
//      try {
//          Job weeklyStatisticsJob = jobRegistry.getJob("weekTop5StatisticsJob");
//          JobParameters jobParameters = new JobParametersBuilder()
//                  .addString("runTime", LocalDateTime.now().toString()) // 매번 새로운 파라미터로 실행
//                  .toJobParameters();
//
//          JobExecution jobExecution = jobLauncher.run(weeklyStatisticsJob, jobParameters);
//          System.out.println("Batch Job Status: " + jobExecution.getStatus());
//      } catch (Exception e) {
//          e.printStackTrace();
//          System.out.println("Error occurred while executing the batch job: " + e.getMessage());
//      }
//  }


   // 월 단위 통계처리
//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//    public void runMonthlyStatisticsJob() {
//        long startTime = System.currentTimeMillis();  // 시작 시간 기록
//        try {
//            Job job = jobRegistry.getJob("monthlyStatisticsJob");
//
//            // 현재 시간을 기반으로 jobParameters 설정 (매 실행 시 고유한 파라미터 생성)
//            jobLauncher.run(job, new JobParametersBuilder()
//                    .addString("startDate", LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_DATE))
//                    .addString("endDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
//                    .addLong("timestamp", System.currentTimeMillis()) // 매번 다른 timestamp로 고유 실행
//                    .toJobParameters());
//
//        } catch (JobExecutionAlreadyRunningException | JobRestartException e) {
//            System.err.println("Job이 이미 실행 중이거나 재시작할 수 없습니다: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Job 실행 중 예외 발생: " + e.getMessage());
//        }
//        long endTime = System.currentTimeMillis();  // 종료 시간 기록
//          long executionTime = endTime - startTime;   // 실행 시간 계산
//
//          System.out.println("Monthly Statistics Job Execution Time: " + executionTime + " ms");
//    }
//
//
//      @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//  public void runMonthlyTop5StatisticsJob() {
//          long startTime = System.currentTimeMillis();  // 시작 시간 기록
//      try {
//          Job weeklyStatisticsJob = jobRegistry.getJob("monthTop5StatisticsJob");
//          JobParameters jobParameters = new JobParametersBuilder()
//                  .addString("runTime", LocalDateTime.now().toString()) // 매번 새로운 파라미터로 실행
//                  .toJobParameters();
//
//          JobExecution jobExecution = jobLauncher.run(weeklyStatisticsJob, jobParameters);
//          System.out.println("Batch Job Status: " + jobExecution.getStatus());
//      } catch (Exception e) {
//          e.printStackTrace();
//          System.out.println("Error occurred while executing the batch job: " + e.getMessage());
//      }
//
//          long endTime = System.currentTimeMillis();  // 종료 시간 기록
//          long executionTime = endTime - startTime;   // 실행 시간 계산
//
//          System.out.println("Monthly Top 5 Statistics Job Execution Time: " + executionTime + " ms");
//  }


//          @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//  public void runDailyTop5StatisticsJob() {
//      try {
//          Job weeklyStatisticsJob = jobRegistry.getJob("dailyTop5StatisticsJob");
//          JobParameters jobParameters = new JobParametersBuilder()
//                  .addString("runTime", LocalDateTime.now().toString()) // 매번 새로운 파라미터로 실행
//                  .toJobParameters();
//
//          JobExecution jobExecution = jobLauncher.run(weeklyStatisticsJob, jobParameters);
//          System.out.println("Batch Job Status: " + jobExecution.getStatus());
//      } catch (Exception e) {
//          e.printStackTrace();
//          System.out.println("Error occurred while executing the batch job: " + e.getMessage());
//      }
//  }


}