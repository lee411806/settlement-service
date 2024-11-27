package com.sparta.settlementservice.service;

import com.sparta.settlementservice.entity.DailyVideoView;
import com.sparta.settlementservice.entity.VideoViewHistory;
import com.sparta.settlementservice.entity.Videos;
import com.sparta.settlementservice.repository.DailyVideoViewRepository;
import com.sparta.settlementservice.repository.VideoRepository;
import com.sparta.settlementservice.repository.VideoViewHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VideoViewHelper {

    private final VideoRepository videoRepository;
    private final VideoViewHistoryRepository videoViewHistoryRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;

    // 조회수 증가
    public void incrementViewCount(Long videoId) {
        LocalDate today = LocalDate.now();
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoIdAndDate(videoId, today)
                .orElseGet(() -> new DailyVideoView(videoId, today)); // 없으면 새로 생성

        dailyVideoView.incrementViewCount(); // 조회수 증가
        dailyVideoViewRepository.save(dailyVideoView);
    }

    // 새로운 시청 기록 생성
    public int createNewHistory(Long userId, Long videoId) {
        System.out.println("createNewHistory 메서드 실행 중 - userId: " + userId + ", videoId: " + videoId);

        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid video ID: " + videoId));

        VideoViewHistory newHistory = new VideoViewHistory();
        newHistory.setUserId(userId);
        newHistory.setVideo(video);
        newHistory.setCurrentPosition(0); // 초기 위치
        newHistory.setLastPlayedDate(LocalDateTime.now());

        videoViewHistoryRepository.save(newHistory);

        System.out.println("VideoViewHistory 저장 완료 - userId: " + newHistory.getUserId());
        return 0; // 새로 생성되었으므로 0초 반환
    }

    // 재생 시간 업데이트
    public void updatePlaytime(Long userId, Long videoId, int currentPosition) {
        VideoViewHistory history = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseThrow(() -> new EntityNotFoundException("시청 기록이 존재하지 않습니다."));

        history.setLastPlayedDate(LocalDateTime.now());
        history.setCurrentPosition(currentPosition);
        videoViewHistoryRepository.save(history); // 시청 기록 저장

        LocalDate today = LocalDate.now();
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoIdAndDate(videoId, today)
                .orElseThrow(() -> new EntityNotFoundException("DailyVideoView 기록이 존재하지 않습니다."));

        dailyVideoView.increasePlaytime(currentPosition); // 재생 시간 추가
        dailyVideoViewRepository.save(dailyVideoView); // 업데이트된 DailyVideoView 저장
    }
}
