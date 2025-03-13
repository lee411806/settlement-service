package com.sparta.settlementservice.streaming.repository;


import com.sparta.settlementservice.streaming.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Videos, Long> {
    Long findVideoLengthById(Long videoId);
}
