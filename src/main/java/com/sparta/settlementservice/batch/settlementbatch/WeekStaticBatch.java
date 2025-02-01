package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.entity.WeekViewPlaytime;
import com.sparta.settlementservice.batch.repo.WeekViewPlaytimeRepository;
import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import com.sparta.settlementservice.streaming.repository.DailyVideoViewRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WeekStaticBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DailyVideoViewRepository dailyVideoViewRepository;
    private final WeekViewPlaytimeRepository weekViewPlaytimeRepository;

    public WeekStaticBatch(JobRepository jobRepository,
                           PlatformTransactionManager platformTransactionManager,
                           DailyVideoViewRepository dailyVideoViewRepository,
                           WeekViewPlaytimeRepository weekViewPlaytimeRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyVideoViewRepository = dailyVideoViewRepository;
        this.weekViewPlaytimeRepository = weekViewPlaytimeRepository;
    }

    // Job 구성
    @Bean
    public Job weeklyStatisticsJob() {
        return new JobBuilder("weeklyStatisticsJob", jobRepository)
                .start(weeklyStatisticsStep())
                .build();
    }

    // Step 구성
    @Bean
    @JobScope
    public Step weeklyStatisticsStep() {
        return new StepBuilder("weeklyStatisticsStep", jobRepository)
                .<DailyVideoView, WeekViewPlaytime>chunk(500, platformTransactionManager) // chunk 크기 설정
                .reader(weeklyStatisticsReader())
                .processor(weeklyStatisticsProcessor())
                .writer(weeklyStatisticsWriter())
                .build();
    }

    // Reader 구성
    @Bean
    @StepScope
    public RepositoryItemReader<DailyVideoView> weeklyStatisticsReader() {

        // 오늘 날짜를 기준으로 해당 주의 월요일과 일요일 계산
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.with(DayOfWeek.MONDAY);  // 이번 주 월요일
        LocalDate endDate = today.with(DayOfWeek.SUNDAY);    // 이번 주 일요일

        return new RepositoryItemReaderBuilder<DailyVideoView>()
                .name("weeklyStatisticsReader")  // 이름 지정
                .repository(dailyVideoViewRepository)
                .methodName("findByDateBetweenOrderByVideoId")
                .arguments(startDate, endDate)
                .pageSize(100) // pageSize 설정
                .sorts(Collections.singletonMap("videoId", Sort.Direction.ASC))
                .build();
    }

    // Processor 구성
    @Bean
    public ItemProcessor<DailyVideoView, WeekViewPlaytime> weeklyStatisticsProcessor() {
        return dailyVideoView -> {
            WeekViewPlaytime weeklyStat = new WeekViewPlaytime();
            weeklyStat.setVideoId(dailyVideoView.getVideoId());
            weeklyStat.setStartDate(dailyVideoView.getDate().with(DayOfWeek.MONDAY));
            weeklyStat.setEndDate(dailyVideoView.getDate().with(DayOfWeek.SUNDAY));
            weeklyStat.setTotalViewCount(dailyVideoView.getViewCount());
            weeklyStat.setTotalPlayTime(dailyVideoView.getPlayTime());
            weeklyStat.setTotalAdViewCount(dailyVideoView.getAdViewCount()); // 광고 조회수 합계 추가
            return weeklyStat;
        };
    }

    // Writer 구성
    @Bean(name = "weekStatisticsLogWriter2")
    public ItemWriter<WeekViewPlaytime> weeklyStatisticsWriter() {
        return items -> {
            Map<Long, WeekViewPlaytime> mergedStats = new HashMap<>();

            for (WeekViewPlaytime item : items) {
                Long videoId = item.getVideoId();
                mergedStats.merge(videoId, item, (existing, newItem) -> {
                    existing.setTotalViewCount(existing.getTotalViewCount() + newItem.getTotalViewCount());
                    existing.setTotalPlayTime(existing.getTotalPlayTime() + newItem.getTotalPlayTime());
                    existing.setTotalAdViewCount(existing.getTotalAdViewCount() + newItem.getTotalAdViewCount()); // 광고 조회수 합계 병합
                    return existing;
                });
            }

            // 최종적으로 병합된 데이터를 데이터베이스에 저장
            weekViewPlaytimeRepository.saveAll(mergedStats.values());
        };
    }
}