package com.sparta.settlementservice.batch.repo.master;

import com.sparta.settlementservice.batch.entity.Top5Statistics;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class Top5StatisticsJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(List<? extends Top5Statistics> statsList) {
        String sql = "INSERT INTO Top5Statistics (videoId, value, startDate, endDate, dateType, staticType) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                statsList,
                statsList.size(),
                (ps, stat) -> {
                    ps.setLong(1, stat.getVideoId());
                    ps.setLong(2, stat.getValue());
                    ps.setObject(3, stat.getStartDate());
                    ps.setObject(4, stat.getEndDate());
                    ps.setString(5, stat.getDateType());
                    ps.setString(6, stat.getStaticType());
                }
        );
    }
}