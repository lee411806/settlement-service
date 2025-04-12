package com.sparta.settlementservice.streaming.service;


import com.sparta.settlementservice.streaming.dto.PauseResponse;
import com.sparta.settlementservice.streaming.dto.PlayRequest;
import com.sparta.settlementservice.streaming.dto.PlayResponse;
import com.sparta.settlementservice.user.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final AbusePreventionHelper abusePreventionHelper; // 어뷰징 헬퍼
    private final VideoViewHelper videoViewHelper; // 조회수 헬퍼
    private final IpHelper ipHelper; // IP 헬퍼
    private final JWTUtil jwtUtil;


    // 동영상 재생 서비스
    public PlayResponse play(Long videoId, HttpServletRequest httpServletRequest, PlayRequest playRequest) {
        String jwtToken = jwtUtil.getTokenFromCookies(httpServletRequest);
        String ipAddress = ipHelper.getClientIp(httpServletRequest); // 헬퍼 사용

        // 출력
        System.out.println("JWT Token: " + jwtToken);
        System.out.println("IP Address: " + ipAddress);


        if (abusePreventionHelper.isAbusiveRequest(jwtToken, ipAddress)) { // 어뷰징 체크
            return new PlayResponse(false, "어뷰징 요청입니다.");
        } else {
            videoViewHelper.createDailyVideoView(videoId, playRequest); // 헬퍼로 시청 기록 생성
            return new PlayResponse(true, "시청 기록 저장 완료");
        }

    }

    // 정지(Pause) 메서드
    public PauseResponse pause(Long userId, Long videoId, Long currentPosition) {

         return videoViewHelper.createVideoViewHistory(userId, videoId, currentPosition);
    }

}