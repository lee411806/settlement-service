package com.sparta.settlementservice.batch.settlementbatch;


import com.sparta.settlementservice.batch.entity.MonthViewPlaytime;
import com.sparta.settlementservice.batch.repo.MonthViewPlaytimeRepository;
import com.sparta.settlementservice.entity.DailyVideoView;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
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

@Configuration
public class MonthStaticBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final MonthViewPlaytimeRepository monthViewPlaytimeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MonthStaticBatch(JobRepository jobRepository,
                            PlatformTransactionManager platformTransactionManager,
                            MonthViewPlaytimeRepository monthViewPlaytimeRepository,
                            JdbcTemplate jdbcTemplate) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.monthViewPlaytimeRepository = monthViewPlaytimeRepository;
        this.jdbcTemplate = jdbcTemplate;
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
                .<DailyVideoView, MonthViewPlaytime>chunk(5000, platformTransactionManager) // chunk 크기 설정
                .reader(monthlyStatisticsReader())
                .processor(monthlyStatisticsProcessor())
                .writer(monthlyStatisticsWriter())
                .taskExecutor(taskExecutor()) // 멀티스레드 적용
                .build();
    }

    // TaskExecutor 설정 (멀티스레드 적용)
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("monthlyStatisticsExecutor-");
        executor.setConcurrencyLimit(10);  // 10개 스레드로 제한
        return executor;
    }

    // Reader 구성
    @Bean
    @StepScope
    public JdbcCursorItemReader<DailyVideoView> monthlyStatisticsReader() {
        // 오늘 날짜를 기준으로 해당 월의 첫 날과 마지막 날 계산
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);  // 이번 달의 첫 날
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth()); // 이번 달의 마지막 날

        return new JdbcCursorItemReaderBuilder<DailyVideoView>()
                .name("monthlyStatisticsReader")
                .dataSource(jdbcTemplate.getDataSource())
                .sql("SELECT * FROM daily_video_view WHERE date BETWEEN ? AND ? ORDER BY video_id")
                .beanRowMapper(DailyVideoView.class)
                .preparedStatementSetter(ps -> {
                    ps.setDate(1, java.sql.Date.valueOf(startDate));
                    ps.setDate(2, java.sql.Date.valueOf(endDate));
                })
                .build();
    }

    // Processor 구성
    @Bean
    public ItemProcessor<DailyVideoView, MonthViewPlaytime> monthlyStatisticsProcessor() {
        return dailyVideoView -> {
            MonthViewPlaytime monthlyStat = new MonthViewPlaytime();
            monthlyStat.setVideoId(dailyVideoView.getVideoId());
            monthlyStat.setStartDate(dailyVideoView.getDate().withDayOfMonth(1)); // 해당 월의 첫 날
            monthlyStat.setEndDate(dailyVideoView.getDate().withDayOfMonth(dailyVideoView.getDate().lengthOfMonth())); // 해당 월의 마지막 날
            monthlyStat.setTotalViewCount(dailyVideoView.getViewCount());
            monthlyStat.setTotalAdViewCount(dailyVideoView.getAdViewCount());  // 광고 조회수 추가
            monthlyStat.setTotalPlayTime(dailyVideoView.getPlayTime());

            return monthlyStat;
        };
    }

    // Writer 구성
    @Bean
    public ItemWriter<MonthViewPlaytime> monthlyStatisticsWriter() {
        return items -> {
            for (MonthViewPlaytime stat : items) {
                // 월별 통계 데이터를 저장
                monthViewPlaytimeRepository.save(stat);
            }
        };
    }
}