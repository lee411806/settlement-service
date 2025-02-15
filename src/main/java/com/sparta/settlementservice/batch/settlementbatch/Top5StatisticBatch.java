package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.dto.VideoViewStats;
import com.sparta.settlementservice.batch.entity.Top5Statistics;
import com.sparta.settlementservice.batch.repo.DailyViewPlaytimeJdbcRepository;
import com.sparta.settlementservice.batch.repo.Top5StatisticsRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class Top5StatisticBatch {

    private final DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository;
    private final Top5StatisticsRepository top5StatisticsRepository;

    public Top5StatisticBatch(
            DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository
            , Top5StatisticsRepository top5StatisticsRepository) {

        this.dailyViewPlaytimeJdbcRepository = dailyViewPlaytimeJdbcRepository;
        this.top5StatisticsRepository = top5StatisticsRepository;
    }

    @Bean
    public Job top5StatisticsBatchJob(JobRepository jobRepository, Step dailyTop5Step) {
        return new JobBuilder("top5StatisticsBatchJob", jobRepository)
                .start(dailyTop5Step)
                .build();
    }

    @Bean
    public Step dailyTop5Step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailyTop5Step", jobRepository)
                .<VideoViewStats, Top5Statistics>chunk(100, transactionManager)
                .reader(dailyTop5Reader())
                .processor(dailyTop5Processor())
                .writer(dailyTop5Writer()) //
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<VideoViewStats> dailyTop5Reader() {
        return new ItemReader<>() {
            private final List<VideoViewStats> buffer = new ArrayList<>();
            private int nextIndex = 0;
            private boolean loaded = false;
            private final LocalDate targetDate = LocalDate.now();


            @Override
            public VideoViewStats read() {
                if (!loaded) {
                    //paging 방식 -> buffer 방식
                    //가져올 데이터 크기가 그렇게 크지 않고, top5만가져오면 되기 때문
                    List<VideoViewStats> viewCountStats = dailyViewPlaytimeJdbcRepository.findTop5ByStatType(targetDate, "VIEW_COUNT");
                    List<VideoViewStats> playTimeStats = dailyViewPlaytimeJdbcRepository.findTop5ByStatType(targetDate, "PLAY_TIME");

                    System.out.println("📌 [ItemReader] 조회된 VIEW_COUNT 데이터 개수: " + viewCountStats.size());
                    System.out.println("📌 [ItemReader] 조회된 PLAY_TIME 데이터 개수: " + playTimeStats.size());
                    buffer.addAll(viewCountStats);
                    buffer.addAll(playTimeStats);
                    loaded = true;
                }
                return nextIndex < buffer.size() ? buffer.get(nextIndex++) : null;
            }
        };
    }

    @Bean
    public ItemProcessor<VideoViewStats, Top5Statistics> dailyTop5Processor() {
        return item -> {
            System.out.println("🔍 [ItemProcessor] 변환 중 - videoId: " + item.getVideoId() + ", totalValue: " + item.getTotalValue());

            return Top5Statistics.builder()
                    .date(LocalDate.now())
                    .dateType("DAILY")
                    .staticType(item.getStatType())
                    .videoId(item.getVideoId())
                    .value(item.getTotalValue())
                    .build();
        };
    }


    @Bean
    public ItemWriter<Top5Statistics> dailyTop5Writer() {
        return items -> {
            System.out.println("🔍 [ItemWriter] 저장할 데이터 개수: " + items.size());

            if (!items.isEmpty()) {
                top5StatisticsRepository.saveAll(items);
                System.out.println("✅ [ItemWriter] 데이터 저장 완료!");
            } else {
                System.out.println("⚠ [ItemWriter] 저장할 데이터가 없음!");
            }
        };
    }
}


