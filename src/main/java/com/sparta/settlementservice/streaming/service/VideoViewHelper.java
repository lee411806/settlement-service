package com.sparta.settlementservice.streaming.service;

import com.sparta.settlementservice.streaming.dto.PlayRequest;
import com.sparta.settlementservice.streaming.entity.DailyVideoView;
import com.sparta.settlementservice.streaming.entity.VideoViewHistory;
import com.sparta.settlementservice.streaming.repository.DailyVideoViewRepository;
import com.sparta.settlementservice.streaming.repository.VideoRepository;
import com.sparta.settlementservice.streaming.repository.VideoViewHistoryRepository;
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

    @Transactional
    public void createVideoViewHistory(Long userId, Long videoId, Long currentPosition) {
        VideoViewHistory history = videoViewHistoryRepository
                .findByUserIdAndVideoId(userId, videoId)
                .orElse(new VideoViewHistory(userId, videoId, currentPosition));

        history.setCurrentPosition(currentPosition);
        videoViewHistoryRepository.save(history);
    }
}
