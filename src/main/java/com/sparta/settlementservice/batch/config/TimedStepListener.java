package com.sparta.settlementservice.batch.config;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class TimedStepListener extends StepExecutionListenerSupport {

    private final long startTime = System.currentTimeMillis();

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long currentTime = System.currentTimeMillis();
        long elapsedTimeMillis = currentTime - startTime;
        double elapsedTimeSeconds = elapsedTimeMillis / 1000.0;

        int readCount = (int) stepExecution.getReadCount();
        double throughput = readCount / elapsedTimeSeconds;

        System.out.println("[Partition] Step: " + stepExecution.getStepName());
        System.out.println("[Partition] Time elapsed: " + elapsedTimeSeconds + " seconds");
        System.out.println("[Partition] Processed items: " + readCount);
        System.out.println("[Partition] Throughput: " + throughput + " items/second");

        return stepExecution.getExitStatus();
    }
}
