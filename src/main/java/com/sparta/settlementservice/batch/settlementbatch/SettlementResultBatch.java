package com.sparta.settlementservice.batch.settlementbatch;

import com.sparta.settlementservice.batch.config.BatchExecutionDecider;
import com.sparta.settlementservice.batch.config.SettlementCalculator;
import com.sparta.settlementservice.batch.dto.SettlementStats;
import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.batch.entity.SettlementResult;
import com.sparta.settlementservice.batch.repo.master.DailyViewPlaytimeJdbcRepository;
import com.sparta.settlementservice.batch.repo.master.SettlementResultJdbcRepository;
import com.sparta.settlementservice.batch.repo.slave.SettlementResultRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
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

@Configuration
public class SettlementResultBatch {

    private final DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository;
    private final SettlementResultRepository settlementResultRepository;
    private final SettlementResultJdbcRepository settlementResultJdbcRepository;

    public SettlementResultBatch(
            DailyViewPlaytimeJdbcRepository dailyViewPlaytimeJdbcRepository
            , SettlementResultRepository settlementResultRepository
            , SettlementResultJdbcRepository settlementResultJdbcRepository
    ) {
        this.dailyViewPlaytimeJdbcRepository = dailyViewPlaytimeJdbcRepository;
        this.settlementResultRepository = settlementResultRepository;
        this.settlementResultJdbcRepository = settlementResultJdbcRepository;
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
                .from(batchExecutionDecider).on("MONTHLY").to(monthlySettlementStep) //  MONTHLY면 monthlySettlementStep 실행
                .from(batchExecutionDecider).on("DAILY").end() // ✅ DAILY → 바로 종료
                .from(batchExecutionDecider).on("*").end() //  예상치 못한 상태에서도 Job이 종료되도록 처리
                .end()
                .build();
    }

    @Bean
    public Step dailySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .<SettlementStats, SettlementResult>chunk(4000, transactionManager) //  DTO 및 엔티티 변경
                .reader(dailySettlementReader()) //  Reader 변경
                .processor(settlementProcessor()) //  Processor 변경
                .writer(settlementWriter()) //  Writer 변경
                .build();
    }

    @Bean
    public Step weeklySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklySettlementStep", jobRepository)
                .<SettlementStats, SettlementResult>chunk(4000, transactionManager)
                .reader(weeklySettlementReader())
                .processor(settlementProcessor()) //  동일한 Processor 사용
                .writer(settlementWriter()) //  동일한 Writer 사용
                .build();
    }

    @Bean
    public Step monthlySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<SettlementStats, SettlementResult>chunk(4000, transactionManager)
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
            private final LocalDate startDate = LocalDate.now(); //
            private final LocalDate endDate = LocalDate.now();


            private List<DailyViewPlaytime> buffer = new ArrayList<>();

            boolean flag = false;

            @Override
            public SettlementStats read() {

                if (!flag) {
                    buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(startDate, endDate, PAGE_SIZE);
                }
                flag = true;

                if (buffer.isEmpty()) {
                    return null;
                }

                //  로그 출력 (데이터 개수 확인)
                System.out.println(" [Reader] 조회된 데이터 개수: " + buffer.size());


                return convertToSettlementStats(buffer.remove(0));
            }

            private SettlementStats convertToSettlementStats(DailyViewPlaytime dailyViewPlaytime) {
                return new SettlementStats(
                        dailyViewPlaytime.getVideoId(),  //  비디오 ID
                        dailyViewPlaytime.getTotalViewCount(),  //  총 조회수
                        dailyViewPlaytime.getTotalAdViewCount(),  //  총 광고 조회수
                        "DAILY",  //  정산 타입 고정
                        dailyViewPlaytime.getCreatedAt(),  //  시작 날짜 (createdAt과 동일)
                        dailyViewPlaytime.getCreatedAt()   //  종료 날짜 (하루 단위이므로 동일)
                );
            }

        };
    }


    @Bean
    @StepScope
    public ItemReader<SettlementStats> weeklySettlementReader() {
        return new ItemReader<>() {
            private static final int PAGE_SIZE = 4000;
            private final LocalDate endDate = LocalDate.now();
            //다음날 새벽에 돌려야해서 원래 +1 해줬으나 , between으로 데이터 조건을 db에서 구분하기 때문에 원래날짜로 설정
            private LocalDate startDate = endDate.minusDays(6); //  월요일
            private LocalDate saveStartDate = endDate.minusDays(6);

            private List<DailyViewPlaytime> buffer = new ArrayList<>();
            boolean flag = false;


            @Override
            public SettlementStats read() {
                if (!flag) {
                    buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(startDate, endDate, PAGE_SIZE);
                    flag = true;
                }
                if (startDate.isAfter(endDate.minusDays(1))) {
                    System.out.println(" [Weekly Reader] 모든 날짜 처리 완료. Step 종료.");
                    return null;
                } else {
                    if (buffer.isEmpty()) {
                        startDate = startDate.plusDays(1);
                        System.out.println(startDate);
                        buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(startDate, endDate, PAGE_SIZE);
                    }
                }


                //  로그 출력 (데이터 개수 확인)
                return convertToSettlementStats(buffer.remove(0),saveStartDate, endDate);
            }

            private SettlementStats convertToSettlementStats(DailyViewPlaytime dailyViewPlaytime,LocalDate saveStartDate,LocalDate endDate) {


                return new SettlementStats(
                        dailyViewPlaytime.getVideoId(),  //  비디오 ID
                        dailyViewPlaytime.getTotalViewCount(),  //  총 조회수
                        dailyViewPlaytime.getTotalAdViewCount(),  //  총 광고 조회수
                        "WEEKLY",  //  정산 타입 고정
                        saveStartDate,   // 시작 날짜
                        endDate  //  종료 날짜 (하루 단위이므로 동일)
                );
            }

        };
    }


    @Bean
    @StepScope
    public ItemReader<SettlementStats> monthlySettlementReader() {
        return new ItemReader<>() {
            private static final int PAGE_SIZE = 4000;
            private final LocalDate endDate = LocalDate.now();
            //  30일 단위로 데이터를 가져오기 위해 startDate 조정
            private LocalDate startDate = endDate.minusDays(29); // 30일 전부터 시작
            private LocalDate saveStartDate = endDate.minusDays(29);

            // videoId별로 누적된 데이터를 저장하는 Map
            Map<Long, SettlementStats> videoStatsMap = new HashMap<>();

            private List<DailyViewPlaytime> buffer = new ArrayList<>();
            boolean flag = false;

            @Override
            public SettlementStats read() {
                if (!flag) {
                    buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(startDate, endDate, PAGE_SIZE);
                    flag = true;
                }

                if (startDate.isAfter(endDate.minusDays(1))) {
                    System.out.println(" [Monthly Reader] 모든 날짜 처리 완료. Step 종료.");
                    return null;
                } else {
                    if (buffer.isEmpty()) {
                        startDate = startDate.plusDays(1);
                        System.out.println(startDate);
                        buffer = dailyViewPlaytimeJdbcRepository.findByDateBetweenOrderByDate(startDate, endDate, PAGE_SIZE);
                    }
                }

                //  로그 출력 (데이터 개수 확인)
                return convertToSettlementStats(buffer.remove(0),saveStartDate, endDate);
            }

            private SettlementStats convertToSettlementStats(DailyViewPlaytime dailyViewPlaytime,LocalDate saveStartDate,LocalDate endDate) {
                return new SettlementStats(
                        dailyViewPlaytime.getVideoId(),  //  비디오 ID
                        dailyViewPlaytime.getTotalViewCount(),  //  총 조회수
                        dailyViewPlaytime.getTotalAdViewCount(),  //  총 광고 조회수
                        "MONTHLY",  //  정산 타입을 "MONTHLY"로 설정
                        saveStartDate,  //  시작 날짜 (createdAt과 동일)
                        endDate   //  종료 날짜 (하루 단위이므로 동일)
                );
            }
        };
    }


    @Bean
    public ItemProcessor<SettlementStats, SettlementResult> settlementProcessor() {
        return stats -> {
//            System.out.println("[ItemProcessor] 처리 중 - videoId: " + stats.getVideoId());

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
        return new ItemWriter<SettlementResult>() {
            private final Map<Long, SettlementResult> videoStatsMap = new HashMap<>();

            @Override
            public void write(Chunk<? extends SettlementResult> chunk) {
                //  1. `chunk`로 받은 데이터 리스트에서 하나씩 처리
                for (SettlementResult stats : chunk.getItems()) {
                    Long videoId = stats.getVideoId();

                    //  2. 이미 videoId가 존재하면 기존 데이터에 값 합산
                    if (videoStatsMap.containsKey(videoId)) {
                        SettlementResult existingStats = videoStatsMap.get(videoId);
                        existingStats.setVideoRevenue(existingStats.getVideoRevenue() + stats.getVideoRevenue());
                        existingStats.setAdRevenue(existingStats.getAdRevenue() + stats.getAdRevenue());
                        existingStats.setTotalRevenue(existingStats.getTotalRevenue() + stats.getTotalRevenue());
                    } else {
                        //  3. videoId가 처음 등장하면 새롭게 추가
                        videoStatsMap.put(videoId, stats);
                    }
                }
            }

            //  Step이 끝난 후 최종 저장
            @AfterStep
            public ExitStatus afterStep(StepExecution stepExecution) {

                List<SettlementResult> finalResults = new ArrayList<>(videoStatsMap.values());
                settlementResultJdbcRepository.saveAllWithDuplicateCheckSettlement(finalResults);
                System.out.println("[ItemWriter] 최종 저장 완료! 저장된 개수: " + finalResults.size());
                videoStatsMap.clear();

                return ExitStatus.COMPLETED;
            }
        };
    }


}
