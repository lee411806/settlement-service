package com.sparta.settlementservice.streaming.service;

import com.sparta.settlementservice.streaming.dto.PauseResponse;
import com.sparta.settlementservice.streaming.repository.VideoRepository;
import com.sparta.settlementservice.streaming.repository.VideoViewHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoViewHelperTest {

    @Mock
    private VideoViewHistoryRepository videoViewHistoryRepository;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoViewHelper videoViewHelper;


    @Test
    void createDailyVideoView() {
    }

    @Test
    void createVideoViewHistory() {
        // given
        Long userId = 1L;
        Long videoId = 100L;
        Long currentPosition = 600L; // 600초 재생
        Long videoLength = 500L;

        when(videoRepository.findVideoLengthById(videoId)).thenReturn(videoLength);

        // when
        PauseResponse response = videoViewHelper.createVideoViewHistory(userId, videoId, currentPosition);

        // then
        assertFalse(response.isSuccess());
        assertEquals("재생 위치가 영상 길이를 초과했습니다.", response.getMessage());
    }
}