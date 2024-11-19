package com.sparta.settlementservice.service;


import com.sparta.settlementservice.dto.AdviewcountRequestDto;
import com.sparta.settlementservice.entity.DailyVideoView;
import com.sparta.settlementservice.entity.ReviewCountAuthentication;
import com.sparta.settlementservice.entity.VideoViewHistory;
import com.sparta.settlementservice.entity.Videos;
import com.sparta.settlementservice.jwt.JwtUtil;
import com.sparta.settlementservice.repository.DailyVideoViewRepository;
import com.sparta.settlementservice.repository.ReviewCountAuthenticationRepository;
import com.sparta.settlementservice.repository.VideoRepository;
import com.sparta.settlementservice.repository.VideoViewHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StreamingService {

    private final VideoViewHistoryRepository videoViewHistoryRepository;
    private final VideoRepository videoRepository;
    private final ReviewCountAuthenticationRepository reviewCountAuthenticationRepository;
    private final DailyVideoViewRepository dailyVideoViewRepository;
    private final JwtUtil jwtUtil;

    // 동영상 재생 서비스
    public int play(Long userId, Long videoId, HttpServletRequest httpServletRequest) {
        String jwtToken = jwtUtil.getJwtFromHeader(httpServletRequest);
        String ipAddress = getClientIp(httpServletRequest);

        if (isAbusiveRequest(jwtToken, ipAddress)) {
            System.out.println("어뷰징입니다.");
            return 0;
        } else {
            Optional<VideoViewHistory> historyOpt = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId);
            if (historyOpt.isPresent()) {
                incrementViewCount(videoId); // 조회수 증가
                return historyOpt.get().getCurrentPosition();
            } else {
                // 시청 기록 생성
                int currentPosition = createNewHistory(userId, videoId);
                incrementViewCount(videoId); // 조회수 증가
                return currentPosition;
            }
        }
    }

    // 어뷰징 방지 메서드
    public boolean isAbusiveRequest(String jwtToken, String ipAddress) {
        Optional<ReviewCountAuthentication> reviewCountAuthentication = reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress);

        int currentTimeInSeconds = (int) (System.currentTimeMillis() / 1000);

        if (reviewCountAuthentication.isPresent()) {
            ReviewCountAuthentication authLog = reviewCountAuthentication.get();
            boolean isAbusive = (currentTimeInSeconds - authLog.getLastActionTime()) <= 30;

            if (isAbusive) {
                authLog.setLastActionTime(currentTimeInSeconds);
                reviewCountAuthenticationRepository.save(authLog);
                System.out.println("어뷰징 요청 감지됨 - lastActionTime 업데이트 완료");
            }

            return isAbusive;
        } else {
            createInitialReviewCountAuthentication(jwtToken, ipAddress, currentTimeInSeconds);
            return false;
        }
    }

    // 어뷰징 기록이 없을 때 새로 생성하는 메서드
    private void createInitialReviewCountAuthentication(String jwtToken, String ipAddress, int currentTimeInSeconds) {
        ReviewCountAuthentication newAuthLog = new ReviewCountAuthentication();
        newAuthLog.setJwtToken(jwtToken);
        newAuthLog.setIpAddress(ipAddress);
        newAuthLog.setLastActionTime(currentTimeInSeconds);
        reviewCountAuthenticationRepository.save(newAuthLog);
        System.out.println("어뷰징 방지 기록 새로 생성 및 저장 완료");
    }

    // 조회 이력이 없는 경우 새로운 기록 생성
    public int createNewHistory(Long userId, Long videoId) {
        System.out.println("createNewHistory 메서드 실행 중 - userId: " + userId + ", videoId: " + videoId);

        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid video ID: " + videoId));

        VideoViewHistory newHistory = new VideoViewHistory();
        newHistory.setUserId(userId);
        newHistory.setVideo(video);
        newHistory.setCurrentPosition(0); // 처음 재생 위치
        newHistory.setLastPlayedDate(LocalDateTime.now());

        videoViewHistoryRepository.save(newHistory);

        System.out.println("VideoViewHistory 저장 완료 - userId: " + newHistory.getUserId());
        return 0; // 새로 생성되었으므로 0초 반환
    }

    // 조회수 증가 메서드
    public void incrementViewCount(Long videoId) {
        LocalDate today = LocalDate.now();
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoIdAndDate(videoId, today)
                .orElseGet(() -> new DailyVideoView(videoId, today)); // 없으면 생성

        dailyVideoView.incrementViewCount();
        dailyVideoViewRepository.save(dailyVideoView);
    }

    // 정지 메서드
    public void pause(Long userId, Long videoId, int currentPosition) {
        VideoViewHistory history = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseThrow(() -> new EntityNotFoundException("시청 기록이 존재하지 않습니다."));

        history.setLastPlayedDate(LocalDateTime.now());
        history.setCurrentPosition(currentPosition);

        LocalDate today = LocalDate.now();
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoIdAndDate(videoId, today)
                .orElseThrow(() -> new EntityNotFoundException("DailyVideoView 기록이 존재하지 않습니다."));

        dailyVideoView.increasePlaytime(currentPosition);
        dailyVideoViewRepository.save(dailyVideoView);
        videoViewHistoryRepository.save(history);
    }

    // 광고 조회수 증가 메서드
    public void adviewcount(AdviewcountRequestDto adviewcountRequestDto) {
        LocalDate today = LocalDate.now();
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoIdAndDate(adviewcountRequestDto.getVideoId(), today)
                .orElseGet(() -> new DailyVideoView(adviewcountRequestDto.getVideoId(), today)); // 없으면 생성

        dailyVideoView.incrementAdViewCount();
        dailyVideoViewRepository.save(dailyVideoView);
    }

    // 클라이언트 IP 주소 가져오기
    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
