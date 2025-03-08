package com.sparta.settlementservice.batch.config;


import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchConfig {
    //TaskExecutor 빈이 하나로만 정의되어 있지만, 파티셔닝을 이용해서
    // 병렬 처리를 하도록 설정하는 것이므로 TaskExecutor가 여러 개로 인식되는 문제는 @Primary 어노테이션으로 해결이 가능
    @Primary
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);  // 최소 4개의 스레드
        executor.setMaxPoolSize(8);   // 최대 8개의 스레드
        executor.setQueueCapacity(100); //스레드 풀이 바쁠 때 최대 100개 작업을 대기열에 넣어둠
        executor.setThreadNamePrefix("batch-executor-"); //스레드 이름 설정
        executor.initialize();
        return executor;
    }


}