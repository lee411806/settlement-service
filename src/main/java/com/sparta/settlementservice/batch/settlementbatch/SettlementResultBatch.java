package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.config.BatchExecutionDecider;
import com.sparta.settlementservice.batch.config.SettlementCalculator;
import com.sparta.settlementservice.batch.dto.SettlementStats;
import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.batch.entity.SettlementResult;
import com.sparta.settlementservice.batch.repo.DailyViewPlaytimeJdbcRepository;
import com.sparta.settlementservice.batch.repo.SettlementResultRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SettlementResultBatch {

    private final DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository;
    private final SettlementResultRepository settlementResultRepository;

    public SettlementResultBatch(
            DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository
            , SettlementResultRepository settlementResultRepository) {
        this.dailyViewPlaytimeJdbcRepository = dailyViewPlaytimeJdbcRepository;
        this.settlementResultRepository = settlementResultRepository;
    }

    @Bean
    public Job settlementResultBatchJob(
            JobRepository jobRepository,
            Step dailySettlementStep,
            Step weeklySettlementStep,
            Step monthlySettlementStep,
            BatchExecutionDecider batchExecutionDecider) {

        return new JobBuilder("settlementResultBatchJob", jobRepository)
                .start(dailySettlementStep) //  DAILY Step은 항상 실행
                .next(batchExecutionDecider) //  Decider 실행 후 상태 값 확인
                .on("WEEKLY").to(weeklySettlementStep) //  WEEKLY면 weeklySettlementStep 실행
                .from(batchExecutionDecider).on("MONTHLY").to(monthlySettlementStep) // ✅ MONTHLY면 monthlySettlementStep 실행
                .end()
                .build();
    }

    @Bean
    public Step dailySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .<SettlementStats, SettlementResult>chunk(100, transactionManager) // ✅ DTO 및 엔티티 변경
                .reader(dailySettlementReader()) //  Reader 변경
                .processor(settlementProcessor()) //  Processor 변경
                .writer(settlementWriter()) //  Writer 변경
                .build();
    }

    @Bean
    public Step weeklySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklySettlementStep", jobRepository)
                .<SettlementStats, SettlementResult>chunk(100, transactionManager)
                .reader(weeklySettlementReader())
                .processor(settlementProcessor()) //  동일한 Processor 사용
                .writer(settlementWriter()) //  동일한 Writer 사용
                .build();
    }

    @Bean
    public Step monthlySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<SettlementStats, SettlementResult>chunk(100, transactionManager)
                .reader(monthlySettlementReader())
                .processor(settlementProcessor()) //  동일한 Processor 사용
                .writer(settlementWriter()) //  동일한 Writer 사용
                .build();
    }


    @Bean
    @StepScope
    public ItemReader<SettlementStats> dailySettlementReader() {
        return new ItemReader<>() {
            private static final int PAGE_SIZE = 4000;
            private final LocalDate startDate = LocalDate.now().minusDays(1);
            private final LocalDate endDate = LocalDate.now();
            private LocalDate currentDate = startDate;
            private List<DailyViewPlaytime> buffer = new ArrayList<>();
            private int index = 0;

            @Override
            public SettlementStats read() {
                // 모든 날짜 처리가 끝났으면 종료
                if (currentDate.isAfter(endDate)) return null;

                // 버퍼가 비었으면 새 데이터를 가져옴
                if (index >= buffer.size()) {
                    buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(currentDate, endDate, PAGE_SIZE);
                    index = 0;

                    // 현재 시점에 데이터가 없으면 다음 날짜로 이동
                    if (buffer.isEmpty()) {
                        currentDate = currentDate.plusDays(1);
                        return read(); // 재귀 호출로 다음 날짜 데이터 시도
                    }
                }

                // 버퍼에서 하나씩 꺼내 SettlementStats로 변환
                DailyViewPlaytime dailyViewPlaytime = buffer.get(index++);

                return new SettlementStats(
                        dailyViewPlaytime.getVideoId(),
                        dailyViewPlaytime.getTotalViewCount(),
                        dailyViewPlaytime.getTotalAdViewCount(),
                        "DAILY", // 정산 타입 (예: DAILY)
                        currentDate, // 시작 날짜
                        currentDate // 종료 날짜 (일 단위니까 동일)
                );
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<SettlementStats> weeklySettlementReader() {
        return new ItemReader<>() {
            private static final int PAGE_SIZE = 4000;
            private final LocalDate startDate = LocalDate.now().minusWeeks(1);
            private final LocalDate endDate = LocalDate.now();
            private List<SettlementStats> buffer = new ArrayList<>(); // ✅ SettlementStats 타입으로 변경
            private int index = 0;

            @Override
            public SettlementStats read() {
                if (index >= buffer.size()) {
                    buffer.clear();

                    // 🔥 DB에서 `DailyViewPlaytime` 조회 후 `SettlementStats`로 변환
                    List<DailyViewPlaytime> rawData = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(startDate, endDate, PAGE_SIZE);

                    if (rawData.isEmpty()) return null;

                    buffer = rawData.stream()
                            .map(playtime -> new SettlementStats(
                                    playtime.getVideoId(),
                                    playtime.getTotalViewCount(),
                                    playtime.getTotalAdViewCount(),
                                    "WEEKLY",
                                    startDate,
                                    endDate
                            ))
                            .collect(Collectors.toList()); // ✅ 변환 후 저장

                    index = 0;
                }

                return index < buffer.size() ? buffer.get(index++) : null;
            }
        };
    }



    @Bean
    @StepScope
    public ItemReader<SettlementStats> monthlySettlementReader() {
        return new ItemReader<>() {
            private static final int PAGE_SIZE = 4000;
            private final LocalDate startDate = LocalDate.now().minusMonths(1); // 1개월 전
            private final LocalDate endDate = LocalDate.now();
            private LocalDate currentDate = startDate;
            private List<DailyViewPlaytime> buffer = new ArrayList<>();
            private int index = 0;

            @Override
            public SettlementStats read() {
                if (currentDate.isAfter(endDate)) return null;

                if (index >= buffer.size()) {
                    buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(currentDate, endDate, PAGE_SIZE);
                    index = 0;

                    if (buffer.isEmpty()) {
                        currentDate = currentDate.plusDays(1);
                        return read();
                    }
                }

                DailyViewPlaytime dailyViewPlaytime = buffer.get(index++);

                return new SettlementStats(
                        dailyViewPlaytime.getVideoId(),
                        dailyViewPlaytime.getTotalViewCount(),
                        dailyViewPlaytime.getTotalAdViewCount(),
                        "MONTHLY", // 정산 타입 (월간)
                        startDate, // 시작 날짜 (1개월 전)
                        endDate    // 종료 날짜 (오늘)
                );
            }
        };
    }

    @Bean
    public ItemProcessor<SettlementStats, SettlementResult> settlementProcessor() {
        return stats -> {
            System.out.println("[ItemProcessor] 처리 중 - videoId: " + stats.getVideoId());

            SettlementCalculator calculator = new SettlementCalculator(); // 직접 객체 생성


            return SettlementResult.builder()
                    .videoId(stats.getVideoId())
                    .videoRevenue(calculator.calculateVideoRevenue(stats.getTotalViewCount()))
                    .adRevenue(calculator.calculateAdRevenue(stats.getTotalAdViewCount()))
                    .totalRevenue(calculator.calculateTotalRevenue(stats.getTotalViewCount(), stats.getTotalAdViewCount()))
                    .startDate(stats.getStartDate())
                    .endDate(stats.getEndDate())
                    .dateType(stats.getDateType())
                    .build();
        };
    }

    @Bean
    public ItemWriter<SettlementResult> settlementWriter() {
        return items -> {
            System.out.println("[ItemWriter] 저장할 데이터 개수: " + items.size());

            if (!items.isEmpty()) {
                settlementResultRepository.saveAll(items);
                System.out.println("[ItemWriter] 데이터 저장 완료!");
            } else {
                System.out.println("[ItemWriter] 저장할 데이터가 없음!");
            }
        };
    }


}
