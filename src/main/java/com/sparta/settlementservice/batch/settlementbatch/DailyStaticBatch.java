package com.sparta.settlementservice.batch.settlementbatch;

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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(dailyStatisticsStep())  // 하나의 Step으로 처리
                .build();
    }

    // Step 구성
    @Bean
    @JobScope
    public Step dailyStatisticsStep() {
        return new StepBuilder("dailyStatisticsStep", jobRepository)
                .partitioner("dailyStatisticsPartitioner", partitioner()) // 파티셔닝 추가
                .step(dailyStatisticsSubStep()) // 하위 Step 실행
                .gridSize(4) // 4개의 파티션으로 나누어 처리
                .taskExecutor(partitionTaskExecutor()) // 병렬 실행을 위한 TaskExecutor
                .build();
    }

    @Bean
    public Step dailyStatisticsSubStep() {
        return new StepBuilder("dailyStatisticsSubStep", jobRepository)
                .<DailyVideoView, DailyViewPlaytime>chunk(1000, platformTransactionManager)
                .reader(dailyStatisticsReader()) // 기존 Reader
                .processor(dailyStatisticsProcessor()) // 기존 Processor
                .writer(dailyStatisticsWriter()) // 기존 Writer
                .build();
    }
    @Bean
    public Partitioner partitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> partitionMap = new HashMap<>();

                // 비디오 ID의 최소 및 최대 범위 설정
                long minVideoId = 1;  // 비디오 ID 최소값
                long maxVideoId = 4000; // 비디오 ID 최대값

                // 각 파티션이 담당할 범위 크기 계산
                long partitionSize = (maxVideoId - minVideoId + 1) / gridSize;

                // 각 파티션에 고유한 lowerBound와 upperBound 할당
                for (int i = 0; i < gridSize; i++) {
                    long lowerBound = minVideoId + (i * partitionSize);
                    long upperBound = (i == gridSize - 1) ? maxVideoId : (lowerBound + partitionSize - 1);

                    ExecutionContext context = new ExecutionContext();
                    context.putLong("lowerBound", lowerBound);
                    context.putLong("upperBound", upperBound);

                    // 파티션 맵에 추가
                    partitionMap.put("partition" + i, context);

                    // 각 파티션 범위 로그 확인
                    System.out.println("Partition " + i + ": Lower Bound = " + lowerBound + ", Upper Bound = " + upperBound);
                }

                return partitionMap;
            }
        };
    }

    @Bean
    public TaskExecutor partitionTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(4); // 병렬 실행 스레드 개수
        return taskExecutor;
    }



    // ItemReader 구성 (한 번에 1000개씩 읽기)
    @Bean
    @StepScope
    public ItemReader<DailyVideoView> dailyStatisticsReader() {
        return new ItemReader<DailyVideoView>() {
            private List<DailyVideoView> currentPageData;
            private int nextIndex = 0;
            private int currentPage = 0;
            private static final int PAGE_SIZE = 1000;

            @Override
            public DailyVideoView read() throws Exception {
                // 데이터가 없으면 새 페이지 데이터를 가져오고, 없으면 null 반환
                if (currentPageData == null || nextIndex >= currentPageData.size()) {
                    Pageable pageable = PageRequest.of(currentPage++, PAGE_SIZE);
                    currentPageData = dailyVideoViewRepository.findByDateWithLimit(LocalDate.of(2024, 11, 1), pageable);

                    if (currentPageData.isEmpty()) {
                        return null;
                    }
                    nextIndex = 0;  // 새 페이지 시작점으로 초기화
                }

                return currentPageData.get(nextIndex++);
            }
        };
    }


    // ItemProcessor 구성 (데이터 처리)
    @Bean
    public ItemProcessor<DailyVideoView, DailyViewPlaytime> dailyStatisticsProcessor() {
        return dailyVideoView -> {
            // 처리된 데이터 반환
            return new DailyViewPlaytime(
                    dailyVideoView.getVideoId(),
                    dailyVideoView.getDate(),
                    dailyVideoView.getViewCount(),
                    dailyVideoView.getAdViewCount(),
                    dailyVideoView.getPlayTime()
            );
        };
    }

    // ItemWriter 구성 (기존 데이터 확인 후 합산 또는 새로 저장)
    @Bean
    public ItemWriter<DailyViewPlaytime> dailyStatisticsWriter() {
        return items -> {
            for (DailyViewPlaytime stat : items) {
                // 기존 데이터 조회 (여러 개의 결과를 반환)
                List<DailyViewPlaytime> existingStats = dailyViewPlaytimeRepository.findByVideoIdAndDate(stat.getVideoId(), stat.getDate());

                if (existingStats != null && !existingStats.isEmpty()) {
                    // 기존 데이터와 합산
                    for (DailyViewPlaytime existingStat : existingStats) {
                        existingStat.setTotalViewCount(existingStat.getTotalViewCount() + stat.getTotalViewCount());
                        existingStat.setTotalAdViewCount(existingStat.getTotalAdViewCount() + stat.getTotalAdViewCount());
                        existingStat.setTotalPlayTime(existingStat.getTotalPlayTime() + stat.getTotalPlayTime());
                    }
                    // 모든 합산된 데이터를 저장
                    dailyViewPlaytimeRepository.saveAll(existingStats);
                } else {
                    // 새 데이터 삽입
                    dailyViewPlaytimeRepository.save(stat);
                }
            }
        };
    }

}