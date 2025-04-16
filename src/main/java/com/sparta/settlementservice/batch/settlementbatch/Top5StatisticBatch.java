package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.config.BatchExecutionDecider;
import com.sparta.settlementservice.batch.dto.VideoViewStats;
import com.sparta.settlementservice.batch.entity.Top5Statistics;
import com.sparta.settlementservice.batch.repo.master.DailyViewPlaytimeJdbcRepository;
import com.sparta.settlementservice.batch.repo.master.Top5StatisticsJdbcRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class Top5StatisticBatch {

    private final DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository;
    private final Top5StatisticsJdbcRepository top5StatisticsJdbcRepository;

    public Top5StatisticBatch(
            DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository
            , BatchExecutionDecider batchExecutionDecider
    , Top5StatisticsJdbcRepository top5StatisticsJdbcRepository) {

        this.dailyViewPlaytimeJdbcRepository = dailyViewPlaytimeJdbcRepository;
        this.top5StatisticsJdbcRepository = top5StatisticsJdbcRepository;
    }

    @Bean
    public Job top5StatisticsBatchJob(
            JobRepository jobRepository,
            Step dailyTop5Step,
            Step weeklyTop5Step,
            Step monthlyTop5Step,
            BatchExecutionDecider batchExecutionDecider) {


        return new JobBuilder("top5StatisticsBatchJob", jobRepository)
                .start(dailyTop5Step) // DAILY Step은 항상 실행
                .next(batchExecutionDecider) // Decider 실행 후 상태 값 확인
                .on("WEEKLY").to(weeklyTop5Step) // WEEKLY면 weeklyTop5Step 실행
                .from(batchExecutionDecider).on("MONTHLY").to(monthlyTop5Step) // MONTHLY면 monthlyTop5Step 실행
                .from(batchExecutionDecider).on("DAILY").end() //  DAILY일 때도 Job 정상 종료
                .from(batchExecutionDecider).on("*").end() //  예상치 못한 상태에서도 Job이 종료되도록 처리
                .end()
                .build();
    }

    @Bean
    public Step dailyTop5Step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailyTop5Step", jobRepository)
                .<VideoViewStats, Top5Statistics>chunk(10
                        , transactionManager)
                .reader(dailyTop5Reader())
                .processor(top5StatisticsProcessor())
                .writer(top5StatisticsWriter()) //
                .build();
    }

    @Bean
    public Step weeklyTop5Step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklyTop5Step", jobRepository)
                .<VideoViewStats, Top5Statistics>chunk(10, transactionManager)
                .reader(weeklyTop5Reader())
                .processor(top5StatisticsProcessor()) // 통합된 Processor 사용
                .writer(top5StatisticsWriter()) // 통합된 Writer 사용
                .build();
    }

    @Bean
    public Step monthlyTop5Step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("monthlyTop5Step", jobRepository)
                .<VideoViewStats, Top5Statistics>chunk(10, transactionManager)
                .reader(monthlyTop5Reader())
                .processor(top5StatisticsProcessor()) // 통합된 Processor 사용
                .writer(top5StatisticsWriter()) // 통합된 Writer 사용
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<VideoViewStats> dailyTop5Reader() {
        return new ItemReader<>() {
            private final List<VideoViewStats> buffer = new ArrayList<>();
            private int nextIndex = 0;
            private boolean loaded = false;
            private final LocalDate startDate = LocalDate.now();
            private final LocalDate endDate = LocalDate.now();

            @Override
            public VideoViewStats read() {
                if (!loaded) {
                    buffer.addAll(dailyViewPlaytimeJdbcRepository.findTop5ByStatType(startDate, endDate, "VIEW_COUNT", "DAILY")
                            .stream()
                            .map(stats -> new VideoViewStats(stats.getVideoId(), stats.getTotalValue(), "VIEW_COUNT", "DAILY", startDate, endDate))
                            .toList());

                    buffer.addAll(dailyViewPlaytimeJdbcRepository.findTop5ByStatType(startDate, endDate, "PLAY_TIME", "DAILY")
                            .stream()
                            .map(stats -> new VideoViewStats(stats.getVideoId(), stats.getTotalValue(), "PLAY_TIME", "DAILY", startDate, endDate))
                            .toList());

                    loaded = true;
                }
                return nextIndex < buffer.size() ? buffer.get(nextIndex++) : null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<VideoViewStats> weeklyTop5Reader() {
        return new ItemReader<>() {
            private final List<VideoViewStats> buffer = new ArrayList<>();
            private int nextIndex = 0;
            private boolean loaded = false;
            private final LocalDate startDate = LocalDate.now().minusDays(6); // 최근 7일
            private final LocalDate endDate = LocalDate.now();

            @Override
            public VideoViewStats read() {
                if (!loaded) {
                    System.out.println("[ItemReader] 주간 Top5 데이터 로딩 시작...");
                    System.out.println("[ItemReader] 조회 기간: " + startDate + " ~ " + endDate);

                    List<VideoViewStats> viewCountStats = dailyViewPlaytimeJdbcRepository.findTop5ByStatType(startDate, endDate, "VIEW_COUNT", "WEEKLY")
                            .stream()
                            .map(stats -> {
                                System.out.println("[ItemReader] VIEW_COUNT 변환 중 - videoId: " + stats.getVideoId() + ", totalValue: " + stats.getTotalValue());
                                return new VideoViewStats(stats.getVideoId(), stats.getTotalValue(), "VIEW_COUNT", "WEEKLY", startDate, endDate);
                            })
                            .toList();

                    List<VideoViewStats> playTimeStats = dailyViewPlaytimeJdbcRepository.findTop5ByStatType(startDate, endDate, "PLAY_TIME", "WEEKLY")
                            .stream()
                            .map(stats -> {
                                System.out.println("[ItemReader] PLAY_TIME 변환 중 - videoId: " + stats.getVideoId() + ", totalValue: " + stats.getTotalValue());
                                return new VideoViewStats(stats.getVideoId(), stats.getTotalValue(), "PLAY_TIME", "WEEKLY", startDate, endDate);
                            })
                            .toList();

                    buffer.addAll(viewCountStats);
                    buffer.addAll(playTimeStats);
                    loaded = true;

                    System.out.println("[ItemReader] 최종 로드된 데이터 개수: " + buffer.size());
                }

                if (nextIndex < buffer.size()) {
                    VideoViewStats item = buffer.get(nextIndex++);
                    System.out.println("[ItemReader] 반환할 데이터 - videoId: " + item.getVideoId() + ", totalValue: " + item.getTotalValue() + ", statType: " + item.getStatType());
                    return item;
                } else {
                    System.out.println("[ItemReader] 데이터 없음 (null 반환)");
                    return null;
                }
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<VideoViewStats> monthlyTop5Reader() {
        return new ItemReader<>() {
            private final List<VideoViewStats> buffer = new ArrayList<>();
            private int nextIndex = 0;
            private boolean loaded = false;
            private final LocalDate startDate = LocalDate.now().minusDays(29); // 최근 30일
            private final LocalDate endDate = LocalDate.now();

            @Override
            public VideoViewStats read() {
                if (!loaded) {
                    buffer.addAll(dailyViewPlaytimeJdbcRepository.findTop5ByStatType(startDate, endDate, "VIEW_COUNT", "MONTHLY")
                            .stream()
                            .map(stats -> new VideoViewStats(stats.getVideoId(), stats.getTotalValue(), "VIEW_COUNT", "MONTHLY", startDate, endDate))
                            .toList());

                    buffer.addAll(dailyViewPlaytimeJdbcRepository.findTop5ByStatType(startDate, endDate, "PLAY_TIME", "MONTHLY")
                            .stream()
                            .map(stats -> new VideoViewStats(stats.getVideoId(), stats.getTotalValue(), "PLAY_TIME", "MONTHLY", startDate, endDate))
                            .toList());

                    loaded = true;
                }
                return nextIndex < buffer.size() ? buffer.get(nextIndex++) : null;
            }
        };
    }


    @Bean
    public ItemProcessor<VideoViewStats, Top5Statistics> top5StatisticsProcessor() {
        return item -> {
            System.out.println(" [ItemProcessor] 변환 중 - videoId: " + item.getVideoId() + ", totalValue: " + item.getTotalValue());
            System.out.println(" [ItemProcessor] dateType 값: " + item.getDateType());
            System.out.println(" [ItemProcessor] startDate: " + item.getStartDate() + ", endDate: " + item.getEndDate());

            return Top5Statistics.builder()
                    .videoId(item.getVideoId())
                    .dateType(item.getDateType())
                    .staticType(item.getStatType())
                    .videoId(item.getVideoId())
                    .value(item.getTotalValue())
                    .startDate(item.getStartDate())
                    .endDate(item.getEndDate())
                    .build();
        };
    }


    @Bean
    public ItemWriter<Top5Statistics> top5StatisticsWriter() {
        return items -> {
            System.out.println(" [ItemWriter] 저장할 데이터 개수: " + items.size());

            if (!items.isEmpty()) {
                top5StatisticsJdbcRepository.saveAll(items.getItems());  //  네가 만든 JDBC 레포지토리 호출
                System.out.println("[ItemWriter] 데이터 저장 완료!");
            } else {
                System.out.println("[ItemWriter] 저장할 데이터가 없음!");
            }
        };

    }
}


