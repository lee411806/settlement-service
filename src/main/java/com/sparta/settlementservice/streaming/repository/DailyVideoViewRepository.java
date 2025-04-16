package com.sparta.settlementservice.streaming.repository;


import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyVideoViewRepository extends JpaRepository<DailyVideoView, Long> {

}
