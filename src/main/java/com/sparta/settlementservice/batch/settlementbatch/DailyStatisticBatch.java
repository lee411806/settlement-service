package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.config.VideoIdPartitioner;
import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.batch.repo.master.DailyViewPlaytimeJdbcRepository;
import com.sparta.settlementservice.streaming.entity.DailyVideoView;
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
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.*;

@Configuration
public class DailyStatisticBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository;
    private final TaskExecutor taskExecutor;

    @Autowired
    public DailyStatisticBatch(JobRepository jobRepository,
                            PlatformTransactionManager platformTransactionManager,
                            DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository,
                            TaskExecutor taskExecutor

    ) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyViewPlaytimeJdbcRepository = dailyViewPlaytimeJdbcRepository;
        this.taskExecutor = taskExecutor;
    }

    // Job 구성
    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(partitionedDailyStatisticsStep()) // 파티션 Step 실행
                .build();
    }


    @Bean
    public Partitioner videoIdPartitioner() {
        return new VideoIdPartitioner(); // 너가 작성한 Partitioner
    }

    @Bean
    public PartitionHandler partitionHandler(TaskExecutor taskExecutor, Step dailyStatisticsStep) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(taskExecutor); // 멀티스레드 실행
        partitionHandler.setStep(dailyStatisticsStep);  // 실행할 Step 지정
        partitionHandler.setGridSize(4); // 병렬 실행 개수 (4개)
        return partitionHandler;
    }

    //파티션 나눔
    @Bean
    @JobScope
    public Step partitionedDailyStatisticsStep() {
        return new StepBuilder("partitionedDailyStatisticsStep", jobRepository)
                .partitioner("dailyStatisticsStep", videoIdPartitioner()) // 파티션 설정
                .step(dailyStatisticsStep()) // 실행할 step 지정
                .partitionHandler(partitionHandler(taskExecutor, dailyStatisticsStep())) // 병렬 실행 (executor가 실제로 스텝 실행)
                .gridSize(4) // 병렬 처리 수 지정
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

        return new ItemReader<>() {
            private Long lastProcessedId = lowerBound; // lowerBound로 초기화
            private static final int PAGE_SIZE = 8000;
            private List<DailyVideoView> currentPageData = new ArrayList<>();
            private int nextIndex = 0;

            @Override
            public DailyVideoView read() throws Exception {

                //한 번에 8000개를 가져오지만, read()가 호출될 때마다 한 개씩 반환하는 방식

                // 종료 조건: Partition 범위 내 데이터를 모두 처리한 경우
                if ((currentPageData.isEmpty() || nextIndex >= currentPageData.size()) && lastProcessedId > upperBound) {
                    System.out.println("[Reader] All data processed for this partition. Terminating.");
                    return null;
                }

                // step 한번당 8000개의 data를 read하는 경우
                if (currentPageData.isEmpty() || nextIndex >= currentPageData.size()) {
                    System.out.println("[Reader] Fetching data between " + lastProcessedId + " and " + upperBound);


                    currentPageData = jdbcRepository.findByVideoIdBetweenOrderByVideoId(
                            lastProcessedId, upperBound, PAGE_SIZE
                    );


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

            // 현재 날짜 가져오기
            LocalDate today = LocalDate.now();

            DailyViewPlaytime existingData = processedDataMap.get(dailyVideoView.getVideoId());
            System.out.println("[Processor] Processing videoId: " + dailyVideoView.getVideoId() + ", date: " + today);
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
                    dailyVideoView.getPlayTime(),
                    today
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
            for (DailyViewPlaytime item : items) {
                System.out.println("[Writer] Writing videoId: " + item.getVideoId() + ", date: " + item.getCreatedAt());
            }
            // DB에 Bulk 저장
            dailyViewPlaytimeJdbcRepository.saveAllWithDuplicateCheck(new ArrayList<>(consolidatedMap.values()));
        };
    }




}