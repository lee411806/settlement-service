package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.entity.Statistics;
import com.sparta.settlementservice.batch.entity.WeekViewPlaytime;
import com.sparta.settlementservice.batch.repo.StatisticsRepository;
import com.sparta.settlementservice.batch.repo.WeekViewPlaytimeRepository;
import org.springframework.batch.core.*;
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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WeekTop5Batch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final WeekViewPlaytimeRepository weekViewPlaytimeRepository;
    private final StatisticsRepository statisticsRepository;

    public WeekTop5Batch(JobRepository jobRepository,
                         PlatformTransactionManager platformTransactionManager,
                         WeekViewPlaytimeRepository weekViewPlaytimeRepository,
                         StatisticsRepository statisticsRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.weekViewPlaytimeRepository = weekViewPlaytimeRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // Job 구성
    @Bean
    public Job weekTop5StatisticsJob() {
        return new JobBuilder("weekTop5StatisticsJob", jobRepository)
                .start(weekTop5ViewStep())
                .next(weekTop5PlaytimeStep())
                .build();
    }

    // 조회수 기준 TOP 5 Step 구성
    @Bean
    @JobScope
    public Step weekTop5ViewStep() {
        return new StepBuilder("weekTop5ViewStep", jobRepository)
                .<WeekViewPlaytime, Statistics>chunk(5, platformTransactionManager)
                .reader(weekTop5ViewReader())
                .processor(weekTop5ViewProcessor())
                .writer(statisticsWriter())
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        System.out.println("weekTop5ViewStep 시작됨");
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        System.out.println("weekTop5ViewStep 완료됨");
                        return ExitStatus.COMPLETED;
                    }
                })
                .build();
    }
    // 재생시간 기준 TOP 5 Step 구성
    @Bean
    @JobScope
    public Step weekTop5PlaytimeStep() {
        return new StepBuilder("weekTop5PlaytimeStep", jobRepository)
                .<WeekViewPlaytime, Statistics>chunk(5, platformTransactionManager)
                .reader(weekTop5PlaytimeReader())
                .processor(weekTop5PlaytimeProcessor())
                .writer(statisticsWriter())
                .build();
    }

    // 조회수 기준 TOP 5 Reader 구성
    @Bean
    @StepScope
    public ItemReader<WeekViewPlaytime> weekTop5ViewReader() {
        List<WeekViewPlaytime> top5ByViewCount = weekViewPlaytimeRepository.findAll().stream()
                .sorted(Comparator.comparing(WeekViewPlaytime::getTotalViewCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
        return new IteratorItemReader<>(top5ByViewCount);
    }

    // 재생시간 기준 TOP 5 Reader 구성
    @Bean
    @StepScope
    public ItemReader<WeekViewPlaytime> weekTop5PlaytimeReader() {
        List<WeekViewPlaytime> top5ByPlayTime = weekViewPlaytimeRepository.findAll().stream()
                .sorted(Comparator.comparing(WeekViewPlaytime::getTotalPlayTime).reversed())
                .limit(5)
                .collect(Collectors.toList());
        return new IteratorItemReader<>(top5ByPlayTime);
    }

    // 조회수 기준 Processor 구성
    @Bean
    public ItemProcessor<WeekViewPlaytime, Statistics> weekTop5ViewProcessor() {
        System.out.println("조회수 스텝 프로세서 시작");
        return weekViewPlaytime -> {
            Statistics statistics = new Statistics();
            statistics.setVideoId(weekViewPlaytime.getVideoId());
            statistics.setStatType("view_count");
            statistics.setPeriodType("week");
            statistics.setStartDate(weekViewPlaytime.getStartDate());
            statistics.setEndDate(weekViewPlaytime.getEndDate());
            statistics.setViewCount(weekViewPlaytime.getTotalViewCount());
            statistics.setPlayTime(0L); // 재생 시간은 조회수 통계에서는 0으로 설정
            return statistics;
        };
    }

    // 재생시간 기준 Processor 구성
    @Bean
    public ItemProcessor<WeekViewPlaytime, Statistics> weekTop5PlaytimeProcessor() {
        System.out.println("플레이타임 스텝 프로세서 시작");
        return weekViewPlaytime -> {
            Statistics statistics = new Statistics();
            statistics.setVideoId(weekViewPlaytime.getVideoId());
            statistics.setStatType("play_time");
            statistics.setPeriodType("week");
            statistics.setStartDate(weekViewPlaytime.getStartDate());
            statistics.setEndDate(weekViewPlaytime.getEndDate());
            statistics.setViewCount(0L); // 조회수는 재생 시간 통계에서는 0으로 설정
            statistics.setPlayTime(weekViewPlaytime.getTotalPlayTime());
            return statistics;
        };
    }

    // Writer 구성
    @Bean(name = "weekStatisticsWriter")
    public ItemWriter<Statistics> statisticsWriter() {
        return items -> {
            items.forEach(stat -> System.out.println("Saving to Statistics - VideoId: " + stat.getVideoId() + ", StatType: " + stat.getStatType() + ", ViewCount: " + stat.getViewCount() + ", PlayTime: " + stat.getPlayTime()));
            statisticsRepository.saveAll(items);
        };
    }


    // Writer 구성 (로그 출력용)
    @Bean(name = "weekStatisticsLogWriter")
    public ItemWriter<Statistics> statisticsLogWriter() {
        return items -> items.forEach(item ->
                System.out.println("Log Writer - Saving Statistics: VideoId: " + item.getVideoId() +
                        ", StatType: " + item.getStatType() +
                        ", PeriodType: " + item.getPeriodType() +
                        ", StartDate: " + item.getStartDate() +
                        ", EndDate: " + item.getEndDate() +
                        ", ViewCount: " + item.getViewCount() +
                        ", PlayTime: " + item.getPlayTime())
        );
    }
}