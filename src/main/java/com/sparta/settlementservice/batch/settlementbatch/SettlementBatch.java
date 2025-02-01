package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.entity.Statistics;
import com.sparta.settlementservice.batch.repo.StatisticsRepository;
import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import com.sparta.settlementservice.streaming.repository.DailyVideoViewRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SettlementBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DailyVideoViewRepository dailyVideoViewRepository;
    private final StatisticsRepository statisticsRepository;

    public SettlementBatch(JobRepository jobRepository,
                           PlatformTransactionManager platformTransactionManager,
                           DailyVideoViewRepository dailyVideoViewRepository,
                           StatisticsRepository statisticsRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyVideoViewRepository = dailyVideoViewRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // 통계 Job 구성: 두 개의 Step을 실행
    @Bean
    public Job statisticsJob() {
        return new JobBuilder("statisticsJob", jobRepository)
                .start(viewCountStep(null)) // view_count Step 실행
                .next(playTimeStep(null)) // play_time Step 실행
                .build();
    }

    // Step 구성 - 조회수(view_count) 상위 5개 처리
    @Bean
    @JobScope
    public Step viewCountStep(@Value("#{jobParameters['period']}") String period) {
        return new StepBuilder("viewCountStep", jobRepository)
                .<Statistics, Statistics>chunk(10, platformTransactionManager)
                .reader(viewCountReader(period))
                .processor(statistics -> statistics)
                .writer(statisticsWriterWithLog())
                .build();
    }

    // Step 구성 - 재생시간(play_time) 상위 5개 처리
    @Bean
    @JobScope
    public Step playTimeStep(@Value("#{jobParameters['period']}") String period) {
        return new StepBuilder("playTimeStep", jobRepository)
                .<Statistics, Statistics>chunk(10, platformTransactionManager)
                .reader(playTimeReader(period))
                .processor(statistics -> statistics)
                .writer(statisticsWriterWithLog())
                .build();
    }

    // 조회수 기준 상위 5개를 가져오는 Reader
    @Bean
    @StepScope
    public ItemReader<Statistics> viewCountReader(@Value("#{jobParameters['period']}") String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = calculateStartDate(period, today);
        LocalDate endDate = calculateEndDate(period, today);

        List<Statistics> statisticsList = new ArrayList<>();
        List<DailyVideoView> top5ByViews = dailyVideoViewRepository.findTop5ByDateBetweenOrderByViewCountDesc(startDate, endDate);

        top5ByViews.forEach(view -> {
            Statistics viewCountStat = new Statistics();
            viewCountStat.setVideoId(view.getVideoId());
            viewCountStat.setPeriodType(period);
            viewCountStat.setStatType("view_count");
            viewCountStat.setViewCount(view.getViewCount());
            statisticsList.add(viewCountStat);
        });

        System.out.println("Reader - Top 5 By Views: " + statisticsList);
        return new IteratorItemReader<>(statisticsList);
    }

    // 재생시간 기준 상위 5개를 가져오는 Reader
    @Bean
    @StepScope
    public ItemReader<Statistics> playTimeReader(@Value("#{jobParameters['period']}") String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = calculateStartDate(period, today);
        LocalDate endDate = calculateEndDate(period, today);

        List<Statistics> statisticsList = new ArrayList<>();
        List<DailyVideoView> top5ByPlaytime = dailyVideoViewRepository.findTop5ByDateBetweenOrderByPlayTimeDesc(startDate, endDate);

        top5ByPlaytime.forEach(playtime -> {
            Statistics playTimeStat = new Statistics();
            playTimeStat.setVideoId(playtime.getVideoId());
            playTimeStat.setPeriodType(period);
            playTimeStat.setStatType("play_time");
            playTimeStat.setPlayTime(playtime.getPlayTime());
            statisticsList.add(playTimeStat);
        });

        System.out.println("Reader - Top 5 By Playtime: " + statisticsList);
        return new IteratorItemReader<>(statisticsList);
    }

    private LocalDate calculateStartDate(String period, LocalDate today) {
        if ("day".equals(period)) {
            return today;
        } else if ("week".equals(period)) {
            return today.with(DayOfWeek.MONDAY);
        } else if ("month".equals(period)) {
            return today.with(TemporalAdjusters.firstDayOfMonth());
        }
        return today;
    }

    private LocalDate calculateEndDate(String period, LocalDate today) {
        if ("day".equals(period)) {
            return today;
        } else if ("week".equals(period)) {
            return today.with(DayOfWeek.SUNDAY);
        } else if ("month".equals(period)) {
            return today.with(TemporalAdjusters.lastDayOfMonth());
        }
        return today;
    }

    // Writer: 처리된 통계 데이터를 저장
    @Bean
    public ItemWriter<Statistics> statisticsWriterWithLog() {
        return items -> {
            items.forEach(stat -> System.out.println("Writer - Saving Statistics: VideoId: " + stat.getVideoId() + ", StatType: " + stat.getStatType()));
            statisticsRepository.saveAll(items);
        };
    }
}
