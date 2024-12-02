package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.config.VideoIdPartitioner;
import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.batch.repo.DailyViewPlaytimeJdbcRepository;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class DailyStaticBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DailyViewPlaytimeRepository dailyViewPlaytimeRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;
    private final DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository;

    @Autowired
    public DailyStaticBatch(JobRepository jobRepository,
                            PlatformTransactionManager platformTransactionManager,
                            DailyViewPlaytimeRepository dailyViewPlaytimeRepository,
                            DailyVideoViewRepository dailyVideoViewRepository,
                            DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository
    ) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyViewPlaytimeRepository = dailyViewPlaytimeRepository;
        this.dailyVideoViewRepository = dailyVideoViewRepository;
        this.dailyViewPlaytimeJdbcRepository = dailyViewPlaytimeJdbcRepository;
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
                .<DailyVideoView, DailyViewPlaytime>chunk(8000, platformTransactionManager)
                .reader(dailyStatisticsReader(null, null,dailyViewPlaytimeJdbcRepository)) // Reader는 ExecutionContext에서 값 주입
                .processor(dailyStatisticsProcessor())
                .writer(dailyStatisticsWriter())
                .build();
    }

    // Reader: 데이터 읽기
    @Bean
    @StepScope
    public ItemReader<DailyVideoView> dailyStatisticsReader(
            @Value("#{stepExecutionContext['lowerBound']}") Long lowerBound,
            @Value("#{stepExecutionContext['upperBound']}") Long upperBound,
            DailyViewPlaytimeJdbcRepository jdbcRepository) {

        System.out.println("[Reader] Initialized with lowerBound=" + lowerBound + ", upperBound=" + upperBound);

        return new ItemReader<DailyVideoView>() {
            private Long lastProcessedId = lowerBound; // lowerBound로 초기화
            private static final int PAGE_SIZE = 8000;
            private List<DailyVideoView> currentPageData = new ArrayList<>();
            private int nextIndex = 0;

            @Override
            public DailyVideoView read() throws Exception {
                // 종료 조건: Partition 범위 내 데이터를 모두 처리한 경우
                if ((currentPageData.isEmpty() || nextIndex >= currentPageData.size()) && lastProcessedId > upperBound) {
                    System.out.println("[Reader] All data processed for this partition. Terminating.");
                    return null;
                }

                // 데이터를 모두 처리한 경우 새로운 데이터를 Fetch
                if (currentPageData.isEmpty() || nextIndex >= currentPageData.size()) {
                    System.out.println("[Reader] Fetching data between " + lastProcessedId + " and " + upperBound);

                    // 데이터를 반복해서 Fetch하며, 같은 videoId를 끝까지 처리
                    currentPageData = jdbcRepository.findByVideoIdBetweenOrderByVideoId(
                            lastProcessedId, upperBound, PAGE_SIZE
                    );

                    // Fetch한 데이터가 없으면 다음 videoId로 넘어가기
                    if (!currentPageData.isEmpty()) {
                        lastProcessedId = currentPageData.get(currentPageData.size() - 1).getVideoId() + 1;
                    }

                    // Fetch한 데이터가 있으면 Index 초기화
                    nextIndex = 0;
                }

                // 현재 데이터 반환
                DailyVideoView item = currentPageData.get(nextIndex++);
                System.out.println("[Reader] Returning videoId: " + item.getVideoId());
                return item;
            }
        };
    }





    @StepScope
    @Bean
    public ItemProcessor<DailyVideoView, DailyViewPlaytime> dailyStatisticsProcessor() {
        Map<Long, DailyViewPlaytime> processedDataMap = new HashMap<>();

        return dailyVideoView -> {
            if (dailyVideoView == null) {
                return null;
            }

            DailyViewPlaytime existingData = processedDataMap.get(dailyVideoView.getVideoId());
            if (existingData != null) {
                // 기존 데이터에 값 합산
                existingData.setTotalViewCount(existingData.getTotalViewCount() + dailyVideoView.getViewCount());
                existingData.setTotalAdViewCount(existingData.getTotalAdViewCount() + dailyVideoView.getAdViewCount());
                existingData.setTotalPlayTime(existingData.getTotalPlayTime() + dailyVideoView.getPlayTime());
                return null; // 중복 데이터는 반환하지 않음
            }

            // 새 데이터 추가
            DailyViewPlaytime newData = new DailyViewPlaytime(
                    dailyVideoView.getVideoId(),
                    dailyVideoView.getViewCount(),
                    dailyVideoView.getAdViewCount(),
                    dailyVideoView.getPlayTime()
            );
            processedDataMap.put(dailyVideoView.getVideoId(), newData);
            return newData;
        };
    }


    @Bean
    @StepScope
    public ItemWriter<DailyViewPlaytime> dailyStatisticsWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }

            // 중복 데이터 합산
            Map<Long, DailyViewPlaytime> consolidatedMap = new HashMap<>();
            for (DailyViewPlaytime item : items) {
                consolidatedMap.merge(item.getVideoId(), item, (existing, newItem) -> {
                    existing.setTotalViewCount(existing.getTotalViewCount() + newItem.getTotalViewCount());
                    existing.setTotalAdViewCount(existing.getTotalAdViewCount() + newItem.getTotalAdViewCount());
                    existing.setTotalPlayTime(existing.getTotalPlayTime() + newItem.getTotalPlayTime());
                    return existing;
                });
            }

            // DB에 Bulk 저장
            dailyViewPlaytimeJdbcRepository.saveAllWithDuplicateCheck(new ArrayList<>(consolidatedMap.values()));
        };
    }




}