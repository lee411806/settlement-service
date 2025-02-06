package com.sparta.settlementservice.streaming.service;

import com.sparta.settlementservice.streaming.dto.PlayRequest;
import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import com.sparta.settlementservice.streaming.entity.VideoViewHistory;
import com.sparta.settlementservice.streaming.repository.DailyVideoViewRepository;
import com.sparta.settlementservice.streaming.repository.VideoRepository;
import com.sparta.settlementservice.streaming.repository.VideoViewHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VideoViewHelper {

    private final VideoRepository videoRepository;
    private final VideoViewHistoryRepository videoViewHistoryRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;

    @Transactional
    public int createDailyVideoView(Long videoId, PlayRequest playRequest) {

        // DTO → Entity 변환
        DailyVideoView dailyVideoView = new DailyVideoView(playRequest, videoId);

        dailyVideoViewRepository.save(dailyVideoView);
        return 0; // 시청 기록이 생성되었거나 업데이트되었음을 반환
    }

    // 재생 시간 업데이트
    public void updatePlaytime(Long userId, Long videoId, Long currentPosition) {
        // 시청기록 존재 확인
        VideoViewHistory history = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseThrow(() -> new EntityNotFoundException("시청 기록이 존재하지 않습니다."));


        // 시청 기록 저장
        history.setLastPlayedDate(LocalDateTime.now());
        history.setCurrentPosition(currentPosition);

        videoViewHistoryRepository.save(history);

    }
}
