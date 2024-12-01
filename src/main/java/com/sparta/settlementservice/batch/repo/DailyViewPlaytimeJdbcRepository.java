package com.sparta.settlementservice.batch.repo;

import com.sparta.settlementservice.batch.entity.DailyViewPlaytime;
import com.sparta.settlementservice.entity.DailyVideoView;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DailyViewPlaytimeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public DailyViewPlaytimeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Find by VideoId range and order by VideoId
    public List<DailyVideoView> findByVideoIdBetweenOrderByVideoId(Long lowerBound, Long upperBound, int pageSize) {
        String sql = "SELECT * FROM daily_video_view " +
                "WHERE video_id BETWEEN ? AND ? " +
                "ORDER BY video_id " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyVideoView(
                rs.getLong("video_id"),
                rs.getLong("view_count"),
                rs.getLong("adview_count"),
                rs.getLong("playtime")
        ), lowerBound, upperBound, pageSize);
    }


    public List<DailyViewPlaytime> findByVideoId(Long videoId) {
        String sql = "SELECT * FROM daily_view_playtime WHERE video_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyViewPlaytime(
                rs.getLong("video_id"),
                rs.getLong("total_view_count"),
                rs.getLong("total_ad_view_count"),
                rs.getLong("total_play_time")
        ), videoId);
    }

    public void saveAllWithDuplicateCheck(List<DailyViewPlaytime> items) {
        if (items.isEmpty()) {
            System.out.println("[saveAllWithDuplicateCheck] No items to process.");
            return;
        }

        // SQL 문 작성 (ON DUPLICATE KEY UPDATE 포함)
        String sql = """
        INSERT INTO daily_view_playtime (video_id, total_view_count, total_ad_view_count, total_play_time)
        VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
        total_view_count = total_view_count + VALUES(total_view_count),
        total_ad_view_count = total_ad_view_count + VALUES(total_ad_view_count),
        total_play_time = total_play_time + VALUES(total_play_time)
    """;

        // 배치 크기 설정 (예: 1000)
        int batchSize = 1000;
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
                    ps.setLong(2, item.getTotalViewCount());
                    ps.setLong(3, item.getTotalAdViewCount());
                    ps.setLong(4, item.getTotalPlayTime());
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


}
