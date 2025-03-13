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

@Component
@RequiredArgsConstructor
public class VideoViewHelper {

    private final VideoRepository videoRepository;
    private final VideoViewHistoryRepository videoViewHistoryRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;

    @Transactional
    public int createDailyVideoView(Long videoId, PlayRequest playRequest) {

        // userId를 jwt 에서 추출해서 userid랑 videoid랑 통해서 currentposition 서버에서 추출함
        // currentposition가져와서 로그데이터에 넣기

        // DTO → Entity 변환
        DailyVideoView dailyVideoView = new DailyVideoView(playRequest, videoId);

        dailyVideoViewRepository.save(dailyVideoView);
        return 0; // 시청 기록이 생성되었거나 업데이트되었음을 반환
    }


    // 동영상 정지 시 currentposition 저장 메서드
    @Transactional
    public void createVideoViewHistory(Long userId, Long videoId, Long currentPosition) {
        Long videoLength = videoRepository.findVideoLengthById(videoId);

        if (videoLength == null) {
            throw new EntityNotFoundException("해당 Video가 존재하지 않습니다.");
        }

        validateCurrentPosition(currentPosition, videoLength); //  비교 로직 유지

        VideoViewHistory history = videoViewHistoryRepository
                .findByUserIdAndVideoId(userId, videoId)
                .orElse(new VideoViewHistory(userId, videoId, currentPosition));

        history.setCurrentPosition(currentPosition);
        videoViewHistoryRepository.save(history);
    }

    // 현재 재생 위치가 영상 길이를 초과하는지 체크
    private void validateCurrentPosition(Long currentPosition, Long videoLength) {
        if (currentPosition > videoLength) {
            throw new IllegalArgumentException("현재 재생 위치가 영상 길이를 초과합니다.");
        }
    }
}
