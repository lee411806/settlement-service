package com.sparta.settlementservice.streaming.repository;


import com.sparta.settlementservice.streaming.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Videos, Long> {
}
