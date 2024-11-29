package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.config.TimedJobExecutionListener;
import com.sparta.settlementservice.batch.config.TimedStepListener;
import com.sparta.settlementservice.batch.config.VideoIdPartitioner;
import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.batch.repo.DailyViewPlaytimeRepository;
import com.sparta.settlementservice.entity.DailyVideoView;
import com.sparta.settlementservice.repository.DailyVideoViewRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class DailyStaticBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DailyViewPlaytimeRepository dailyViewPlaytimeRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;

    @Autowired
    public DailyStaticBatch(JobRepository jobRepository,
                            PlatformTransactionManager platformTransactionManager,
                            DailyViewPlaytimeRepository dailyViewPlaytimeRepository,
                            DailyVideoViewRepository dailyVideoViewRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyViewPlaytimeRepository = dailyViewPlaytimeRepository;
        this.dailyVideoViewRepository = dailyVideoViewRepository;
    }

    // Job 구성
    @Bean
    public Job dailyStatisticsJob() {
        System.out.println("[Job] Starting 'dailyStatisticsJob'");
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(partitionedDailyStatisticsStep()) // 파티션 Step 실행
                .build();
    }


    @Bean
    public Partitioner videoIdPartitioner() {
        System.out.println("[Partitioner] Initialized VideoIdPartitioner");
        return new VideoIdPartitioner(); // 너가 작성한 Partitioner
    }

    //TaskExecutor 빈이 하나로만 정의되어 있지만, 파티셔닝을 이용해서
    // 병렬 처리를 하도록 설정하는 것이므로 TaskExecutor가 여러 개로 인식되는 문제는 @Primary 어노테이션으로 해결이 가능
    @Primary
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 최소 4개의 스레드
        executor.setMaxPoolSize(8);  // 최대 8개의 스레드
        executor.setQueueCapacity(100); // 대기 큐의 크기
        executor.setThreadNamePrefix("batch-executor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public PartitionHandler partitionHandler(TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(taskExecutor); // 병렬 처리할 TaskExecutor 지정
        partitionHandler.setStep(dailyStatisticsStep()); // 각 파티션에 대해 실행할 Step 지정
        partitionHandler.setGridSize(4); // 병렬 처리 수 (4개의 파티션)
        return partitionHandler;
    }

    //파티션 나눔
    @Bean
    @JobScope
    public Step partitionedDailyStatisticsStep() {
        System.out.println("[Step] Starting 'partitionedDailyStatisticsStep'");
        return new StepBuilder("partitionedDailyStatisticsStep", jobRepository)
                .partitioner("dailyStatisticsStep", videoIdPartitioner()) // 파티션 설정
                .step(dailyStatisticsStep()) // 기존 Step 재사용
                .partitionHandler(partitionHandler(taskExecutor())) // 병렬 처리 핸들러 추가
                .gridSize(4) // 병렬 처리 수 (4개의 파티션)
                .build();
    }

    // Step 구성
    @Bean
    @StepScope
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Step dailyStatisticsStep() {
        return new StepBuilder("dailyStatisticsStep", jobRepository)
                .<DailyVideoView, DailyViewPlaytime>chunk(1000, platformTransactionManager)
                .reader(dailyStatisticsReader(null, null)) // Reader는 ExecutionContext에서 값 주입
                .processor(dailyStatisticsProcessor())
                .writer(dailyStatisticsWriter())
                .build();
    }
    @Bean
    public Job timedJob(TimedJobExecutionListener timedJobExecutionListener) {
        return new JobBuilder("timedJob", jobRepository)
                .listener(timedJobExecutionListener) // Job Listener 등록
                .start(partitionedDailyStatisticsStep())
                .build();
    }
    @Bean
    @JobScope
    public Step partitionedDailyStatisticsStepWithLogging(TimedStepListener timedStepListener) {
        return new StepBuilder("partitionedDailyStatisticsStep", jobRepository)
                .partitioner("dailyStatisticsStep", videoIdPartitioner()) // 파티션 설정
                .step(dailyStatisticsStep()) // Step 재사용
                .listener(timedStepListener) // Step Listener 등록
                .partitionHandler(partitionHandler(taskExecutor())) // 병렬 처리 핸들러
                .build();
    }
    // Reader: 데이터 읽기
    @Bean
    @StepScope
    public ItemReader<DailyVideoView> dailyStatisticsReader(
            @Value("#{stepExecutionContext['lowerBound']}") Long lowerBound,
            @Value("#{stepExecutionContext['upperBound']}") Long upperBound) {

        System.out.println("[Reader] Initialized with lowerBound=" + lowerBound + ", upperBound=" + upperBound);

        return new ItemReader<DailyVideoView>() {
            private Long lastProcessedId = lowerBound; // lowerBound로 초기화
            private static final int PAGE_SIZE = 1000;
            private List<DailyVideoView> currentPageData;
            private int nextIndex = 0;

            @Override
            public DailyVideoView read() throws Exception {
                if (currentPageData == null || nextIndex >= currentPageData.size()) {
                    System.out.println("[Reader] Fetching data between " + lastProcessedId + " and " + upperBound);

                    currentPageData = dailyVideoViewRepository.findByVideoIdBetweenOrderByVideoId(
                            lastProcessedId, upperBound, PageRequest.of(0, PAGE_SIZE)
                    );

                    if (currentPageData.isEmpty()) {
                        return null; // 더 이상 데이터가 없으면 종료
                    }

                    lastProcessedId = currentPageData.get(currentPageData.size() - 1).getVideoId();
                    nextIndex = 0;
                }
                return currentPageData.get(nextIndex++);
            }
        };
    }

    @Bean
    public ItemProcessor<DailyVideoView, DailyViewPlaytime> dailyStatisticsProcessor() {
        return dailyVideoView -> {
            if (dailyVideoView == null) {
                return null;
            }

            // 기존 데이터 조회 (videoId 기준)
            List<DailyViewPlaytime> existingDataList = dailyViewPlaytimeRepository.findByVideoId(
                    dailyVideoView.getVideoId() // 이제 date 기준을 빼고 videoId만 사용
            );

            if (existingDataList != null && !existingDataList.isEmpty()) {
                // 여러 개의 데이터가 있으면 합산 (기존 데이터를 갱신)
                DailyViewPlaytime existingData = existingDataList.get(0); // 첫 번째 요소만 사용
                existingData.setTotalViewCount(existingData.getTotalViewCount() + dailyVideoView.getViewCount());
                existingData.setTotalAdViewCount(existingData.getTotalAdViewCount() + dailyVideoView.getAdViewCount());
                existingData.setTotalPlayTime(existingData.getTotalPlayTime() + dailyVideoView.getPlayTime());

                System.out.println("Processor updated existing data for videoId: " + dailyVideoView.getVideoId());
                return existingData;
            } else {
                // 새 데이터 생성
                DailyViewPlaytime newData = new DailyViewPlaytime(
                        dailyVideoView.getVideoId(),
                        dailyVideoView.getDate(),
                        dailyVideoView.getViewCount(),
                        dailyVideoView.getAdViewCount(),
                        dailyVideoView.getPlayTime()
                );

                System.out.println("Processor created new data for videoId: " + dailyVideoView.getVideoId());
                return newData;
            }
        };
    }


    // Writer: 데이터 저장
    @Bean
    public ItemWriter<DailyViewPlaytime> dailyStatisticsWriter() {
        return items -> {
            if (items.isEmpty()) {
                System.out.println("Writer: No data to write.");
                return;
            }

            System.out.println("Writer: Writing " + items.size() + " items.");
            for (DailyViewPlaytime item : items) {
                // 데이터베이스에 저장 전에 중복 확인
                if (!dailyViewPlaytimeRepository.existsByVideoIdAndDate(item.getVideoId(), item.getDate())) {
                    dailyViewPlaytimeRepository.save(item); // 중복되지 않으면 저장
                }
            }
        };
    }
}