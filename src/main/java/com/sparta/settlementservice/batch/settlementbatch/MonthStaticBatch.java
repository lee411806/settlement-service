package com.sparta.settlementservice.batch.settlementbatch;


import com.sparta.settlementservice.batch.entity.MonthViewPlaytime;
import com.sparta.settlementservice.batch.repo.MonthViewPlaytimeRepository;
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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class MonthStaticBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final MonthViewPlaytimeRepository monthViewPlaytimeRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;

    @Autowired
    public MonthStaticBatch(JobRepository jobRepository,
                            PlatformTransactionManager platformTransactionManager,
                            MonthViewPlaytimeRepository monthViewPlaytimeRepository,
                            DailyVideoViewRepository dailyVideoViewRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.monthViewPlaytimeRepository = monthViewPlaytimeRepository;
        this.dailyVideoViewRepository = dailyVideoViewRepository;
    }

    // Job 구성
    @Bean
    public Job monthlyStatisticsJob() {
        return new JobBuilder("monthlyStatisticsJob", jobRepository)
                .start(monthlyStatisticsStep())
                .build();
    }

    // Step 구성
    @Bean
    @JobScope
    public Step monthlyStatisticsStep() {
        return new StepBuilder("monthlyStatisticsStep", jobRepository)
                .<DailyVideoView, MonthViewPlaytime>chunk(100, platformTransactionManager) // 청크 크기 조정
                .reader(monthlyStatisticsReader())
                .processor(monthlyStatisticsProcessor())
                .writer(monthlyStatisticsWriter())
                .build();
    }

    // Reader 구성
    @Bean
    @StepScope
    public ItemReader<DailyVideoView> monthlyStatisticsReader() {
        return new ItemReader<>() {
            private List<DailyVideoView> dailyVideoViews;
            private int nextIndex;

            @Override
            public DailyVideoView read() throws Exception {
                if (dailyVideoViews == null) {
                    LocalDate today = LocalDate.now();
                    LocalDate startDate = today.withDayOfMonth(1);
                    LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

                    // JPA Repository 호출
                    dailyVideoViews = dailyVideoViewRepository.findByDateBetween(startDate, endDate);
                    nextIndex = 0;
                }

                if (nextIndex < dailyVideoViews.size()) {
                    return dailyVideoViews.get(nextIndex++);
                }
                return null; // 모든 데이터 읽기 완료
            }
        };
    }

    // Processor 구성
    // Processor 수정
    @Bean
    public ItemProcessor<DailyVideoView, MonthViewPlaytime> monthlyStatisticsProcessor() {
        // videoId별 데이터를 그룹화하기 위한 Map
        Map<Long, MonthViewPlaytime> groupedStats = new HashMap<>();

        return dailyVideoView -> {
            Long videoId = dailyVideoView.getVideoId();

            // Map에 없는 경우 초기화
            groupedStats.putIfAbsent(videoId, new MonthViewPlaytime(
                    videoId,
                    dailyVideoView.getDate().withDayOfMonth(1),  // 해당 월의 첫 날
                    dailyVideoView.getDate().withDayOfMonth(dailyVideoView.getDate().lengthOfMonth()),  // 해당 월의 마지막 날
                    0L, 0L, 0L // 초기 값
            ));

            // 기존 데이터 가져와 합산
            MonthViewPlaytime stat = groupedStats.get(videoId);
            stat.setTotalViewCount(stat.getTotalViewCount() + dailyVideoView.getViewCount());
            stat.setTotalAdViewCount(stat.getTotalAdViewCount() + dailyVideoView.getAdViewCount());
            stat.setTotalPlayTime(stat.getTotalPlayTime() + dailyVideoView.getPlayTime());

            // 현재 아이템에 해당하는 결과 반환
            return stat;
        };
    }



    // Writer 구성
    @Bean
    public ItemWriter<MonthViewPlaytime> monthlyStatisticsWriter() {
        return items -> {
            for (MonthViewPlaytime stat : items) {
                // DB에 기존 데이터가 있는지 확인
                MonthViewPlaytime existingStat = monthViewPlaytimeRepository.findByVideoIdAndStartDateAndEndDate(
                        stat.getVideoId(),
                        stat.getStartDate(),
                        stat.getEndDate()
                );

                if (existingStat != null) {
                    // 기존 데이터가 있다면 값 누적
                    existingStat.setTotalViewCount(existingStat.getTotalViewCount() + stat.getTotalViewCount());
                    existingStat.setTotalAdViewCount(existingStat.getTotalAdViewCount() + stat.getTotalAdViewCount());
                    existingStat.setTotalPlayTime(existingStat.getTotalPlayTime() + stat.getTotalPlayTime());
                    monthViewPlaytimeRepository.save(existingStat);
                } else {
                    // 기존 데이터가 없으면 새로 삽입
                    monthViewPlaytimeRepository.save(stat);
                }
            }
        };
    }
}