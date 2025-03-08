package com.sparta.settlementservice.batch.repo.master;

import com.sparta.settlementservice.batch.entity.SettlementResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SettlementResultJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public SettlementResultJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //settlement 결과 저장
    public void saveAllWithDuplicateCheckSettlement(List<SettlementResult> items) {
        if (items.isEmpty()) return; // ✅ 저장할 데이터 없으면 바로 종료

        String sql = """
                INSERT INTO settlementResult (videoId, videoRevenue, adRevenue, totalRevenue, startDate, endDate, dateType)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                videoRevenue = VALUES(videoRevenue),
                adRevenue = VALUES(adRevenue),
                totalRevenue = VALUES(totalRevenue),
                startDate = VALUES(startDate),
                endDate = VALUES(endDate),
                dateType = VALUES(dateType)
            """;

        jdbcTemplate.batchUpdate(sql, items, items.size(), (ps, item) -> {
            ps.setLong(1, item.getVideoId());
            ps.setLong(2, item.getVideoRevenue());
            ps.setLong(3, item.getAdRevenue());
            ps.setLong(4, item.getTotalRevenue());
            ps.setObject(5, item.getStartDate()); // LocalDate → DATE
            ps.setObject(6, item.getEndDate()); // LocalDate → DATE
            ps.setString(7, item.getDateType());
        });

        System.out.println("[INFO] Bulk Insert 완료! 저장 개수: " + items.size());
    }


}
