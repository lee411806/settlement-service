package com.sparta.settlementservice.batch.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class BatchExecutionDecider implements JobExecutionDecider {

    //FlowExecutionStatus: Spring Batch에서 Step의 실행 상태를 결정하는 객체
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
      LocalDate today = LocalDate.of(2025, 2, 24);
//        LocalDate today = LocalDate.now();
        System.out.println(" [Decider] 실행됨! JobExecution ID: " + jobExecution.getId());
        if (isMonday(today) && isFirstDayOfMonth(today)) {
            System.out.println(" [Decider] 결과: WEEKLY_MONTHLY");
            return new FlowExecutionStatus("WEEKLY_MONTHLY"); //  월요일 + 1일 → 둘 다 실행
        } else if (isFirstDayOfMonth(today)) {
            System.out.println(" [Decider] 결과: MONTHLY");
            return new FlowExecutionStatus("MONTHLY"); //  1일이면 MONTHLY 실행
        } else if (isMonday(today)) {
            System.out.println(" [Decider] 결과: WEEKLY");
            return new FlowExecutionStatus("WEEKLY"); //  월요일이면 WEEKLY 실행
        }

        System.out.println(" [Decider] 결과: DAILY");
        return new FlowExecutionStatus("DAILY");

    }

    private boolean isMonday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.MONDAY;
    }

    private boolean isFirstDayOfMonth(LocalDate date) {
        boolean result = date.getDayOfMonth() == 1;
        System.out.println(" [Decider] isFirstDayOfMonth(" + date + ") = " + result);
        return date.getDayOfMonth() == 1;
    }
}

