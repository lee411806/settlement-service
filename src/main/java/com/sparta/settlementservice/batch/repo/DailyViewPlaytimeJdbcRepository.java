package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.dto.VideoViewStats;
import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class DailyViewPlaytimeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public DailyViewPlaytimeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Find by VideoId range and order by VideoId
    public List<DailyVideoView> findByVideoIdBetweenOrderByVideoId(Long lowerBound, Long upperBound, int pageSize) {
        String sql = "SELECT * FROM dailyVideoView " +
                "WHERE videoId BETWEEN ? AND ? " +
                "ORDER BY videoId " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyVideoView(
                rs.getLong("videoId"),
                rs.getLong("viewCount"),
                rs.getLong("adViewCount"),
                rs.getLong("playTime"),
                rs.getLong("currentPosition") // 추가된 필드 매핑
        ), lowerBound, upperBound, pageSize);
    }

    public void saveAllWithDuplicateCheck(List<DailyViewPlaytime> items) {
        if (items.isEmpty()) {
            System.out.println("[saveAllWithDuplicateCheck] No items to process.");
            return;
        }

        // SQL 문 작성 (ON DUPLICATE KEY UPDATE 포함)
        String sql = """
        INSERT INTO dailyViewPlaytime (videoId, date, totalViewCount, totalAdViewCount, totalPlayTime)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
        date = VALUES(date), 
        totalViewCount = totalViewCount + VALUES(totalViewCount),
        totalAdViewCount = totalAdViewCount + VALUES(totalAdViewCount),
        totalPlayTime = totalPlayTime + VALUES(totalPlayTime)
    """;

        // 배치 크기 설정 (예: 1000)
        int batchSize = 8000;
        int totalSize = items.size();
        int processed = 0;

        try {
            while (processed < totalSize) {
                // 현재 배치에 포함할 데이터 계산
                int endIndex = Math.min(processed + batchSize, totalSize);
                List<DailyViewPlaytime> batchItems = items.subList(processed, endIndex);

                // 배치 처리
                jdbcTemplate.batchUpdate(sql, batchItems, batchItems.size(), (ps, item) -> {
                    ps.setLong(1, item.getVideoId());
                    ps.setObject(2, item.getDate()); // 🔥 `date` 값 추가
                    ps.setLong(3, item.getTotalViewCount());
                    ps.setLong(4, item.getTotalAdViewCount());
                    ps.setLong(5, item.getTotalPlayTime());
                });

                System.out.println("[saveAllWithDuplicateCheck] Processed batch: " + (processed + 1) + " to " + endIndex);
                processed = endIndex; // 다음 배치로 이동
            }

            System.out.println("[saveAllWithDuplicateCheck] Bulk updated " + items.size() + " records.");
        } catch (Exception e) {
            // 예외 처리 추가
            System.err.println("[saveAllWithDuplicateCheck] Error during batch update: " + e.getMessage());
            throw new RuntimeException("Batch update failed", e);
        }
    }


    // 여기서 부터 Top5 배치 Jdbc 활용
    // ✅ 하나의 메서드에서 statType에 따라 쿼리 실행
    public List<VideoViewStats> findTop5ByStatType(LocalDate targetDate, String statType) {
        String orderByColumn = statType.equals("VIEW_COUNT") ? "SUM(viewCount)" : "SUM(playTime)";

        String sql = "SELECT videoId, " + orderByColumn + " AS totalValue " +
                "FROM dailyVideoView " +
                "WHERE DATE(createdAt) = ? " +  // ✅ 특정 날짜만 조회하도록 수정
                "GROUP BY videoId " +
                "ORDER BY totalValue DESC " +
                "LIMIT 5";  // ✅ TOP 5 고정

        return jdbcTemplate.query(sql, (rs, rowNum) -> new VideoViewStats(
                rs.getLong("videoId"),
                rs.getLong("totalValue"),
                statType
        ), targetDate);
    }




}
