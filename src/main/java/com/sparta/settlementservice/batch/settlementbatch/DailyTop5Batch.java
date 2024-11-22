package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.entity.Statistics;
import com.sparta.settlementservice.batch.repo.StatisticsRepository;
import com.sparta.settlementservice.entity.DailyVideoView;
import com.sparta.settlementservice.repository.DailyVideoViewRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DailyTop5Batch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DailyVideoViewRepository dailyVideoViewRepository;
    private final StatisticsRepository statisticsRepository;

    public DailyTop5Batch(JobRepository jobRepository,
                          PlatformTransactionManager platformTransactionManager,
                          DailyVideoViewRepository dailyVideoViewRepository,
                          StatisticsRepository statisticsRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyVideoViewRepository = dailyVideoViewRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // Job 구성
    @Bean
    public Job dailyTop5StatisticsJob() {
        return new JobBuilder("dailyTop5StatisticsJob", jobRepository)
                .start(dailyTop5ViewStep())
                .next(dailyTop5PlaytimeStep())
                .build();
    }

    // 조회수 기준 TOP 5 Step 구성
    @Bean
    @JobScope
    public Step dailyTop5ViewStep() {
        return new StepBuilder("dailyTop5ViewStep", jobRepository)
                .<DailyVideoView, Statistics>chunk(5, platformTransactionManager)
                .reader(dailyTop5ViewReader())
                .processor(dailyTop5ViewProcessor())
                .writer(dailyStatisticsWriter())
                .build();
    }

    // 재생시간 기준 TOP 5 Step 구성
    @Bean
    @JobScope
    public Step dailyTop5PlaytimeStep() {
        return new StepBuilder("dailyTop5PlaytimeStep", jobRepository)
                .<DailyVideoView, Statistics>chunk(5, platformTransactionManager)
                .reader(dailyTop5PlaytimeReader())
                .processor(dailyTop5PlaytimeProcessor())
                .writer(dailyStatisticsWriter())
                .build();
    }

    // 오늘 날짜의 조회수 기준 TOP 5 Reader
    @Bean
    @StepScope
    public ItemReader<DailyVideoView> dailyTop5ViewReader() {
        List<DailyVideoView> top5ByViewCount = dailyVideoViewRepository.findByDate(LocalDate.now()).stream()
                .sorted(Comparator.comparing(DailyVideoView::getViewCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
        return new IteratorItemReader<>(top5ByViewCount);
    }

    // 오늘 날짜의 재생시간 기준 TOP 5 Reader
    @Bean
    @StepScope
    public ItemReader<DailyVideoView> dailyTop5PlaytimeReader() {
        List<DailyVideoView> top5ByPlayTime = dailyVideoViewRepository.findByDate(LocalDate.now()).stream()
                .sorted(Comparator.comparing(DailyVideoView::getPlayTime).reversed())
                .limit(5)
                .collect(Collectors.toList());
        return new IteratorItemReader<>(top5ByPlayTime);
    }

    // 조회수 기준 Processor 구성
    @Bean
    public ItemProcessor<DailyVideoView, Statistics> dailyTop5ViewProcessor() {
        return dailyView -> {
            Statistics statistics = new Statistics();
            statistics.setVideoId(dailyView.getVideoId());
            statistics.setStatType("view_count");
            statistics.setPeriodType("day");
            statistics.setStartDate(dailyView.getDate());
            statistics.setEndDate(dailyView.getDate());
            statistics.setViewCount(dailyView.getViewCount());
            statistics.setPlayTime(0L); // 조회수 기준은 재생 시간 0
            return statistics;
        };
    }

    // 재생시간 기준 Processor 구성
    @Bean
    public ItemProcessor<DailyVideoView, Statistics> dailyTop5PlaytimeProcessor() {
        return dailyView -> {
            Statistics statistics = new Statistics();
            statistics.setVideoId(dailyView.getVideoId());
            statistics.setStatType("play_time");
            statistics.setPeriodType("day");
            statistics.setStartDate(dailyView.getDate());
            statistics.setEndDate(dailyView.getDate());
            statistics.setViewCount(0L); // 재생 시간 기준은 조회수 0
            statistics.setPlayTime(dailyView.getPlayTime());
            return statistics;
        };
    }

    // Writer 구성
    @Bean
    public ItemWriter<Statistics> dailyStatisticsWriter() {
        return items -> {
            items.forEach(stat -> System.out.println("Log Writer - Saving to Statistics: VideoId: " + stat.getVideoId() + ", StatType: " + stat.getStatType() + ", ViewCount: " + stat.getViewCount() + ", PlayTime: " + stat.getPlayTime()));
            statisticsRepository.saveAll(items);
        };
    }
}