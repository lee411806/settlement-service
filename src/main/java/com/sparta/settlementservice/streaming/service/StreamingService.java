package com.sparta.settlementservice.streaming.service;


import com.sparta.settlementservice.streaming.dto.PlayRequest;
import com.sparta.settlementservice.user.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StreamingService {

    private final AbusePreventionHelper abusePreventionHelper; // 어뷰징 헬퍼
    private final VideoViewHelper videoViewHelper; // 조회수 헬퍼
    private final IpHelper ipHelper; // IP 헬퍼
    private final JWTUtil jwtUtil;


    // 동영상 재생 서비스
    public int play(Long videoId, HttpServletRequest httpServletRequest, PlayRequest playRequest) {
        String jwtToken = jwtUtil.getTokenFromCookies(httpServletRequest);
        String ipAddress = ipHelper.getClientIp(httpServletRequest); // 헬퍼 사용

        // 출력
        System.out.println("JWT Token: " + jwtToken);
        System.out.println("IP Address: " + ipAddress);

        if (abusePreventionHelper.isAbusiveRequest(jwtToken, ipAddress)) { // 어뷰징 체크
            System.out.println("어뷰징입니다.");
            return 0;
        } else {
            return videoViewHelper.createDailyVideoView(videoId,playRequest); // 헬퍼로 시청 기록 생성
        }
    }

    // 정지(Pause) 메서드
    public void pause(Long userId, Long videoId, Long currentPosition) {
        videoViewHelper.createVideoViewHistory(userId, videoId, currentPosition);
    }

    //jwtutil에서 userid 가져오는 방법


}