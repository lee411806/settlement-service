package com.sparta.settlementservice.service;


import com.sparta.settlementservice.dto.AdviewcountRequestDto;
import com.sparta.settlementservice.userservice.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class StreamingService {

    private final AbusePreventionHelper abusePreventionHelper; // 어뷰징 헬퍼
    private final VideoViewHelper videoViewHelper; // 조회수 헬퍼
    private final IpHelper ipHelper; // IP 헬퍼
    private final JwtUtil jwtUtil;

    // 동영상 재생 서비스
    public int play(Long videoId, HttpServletRequest httpServletRequest) {
        String jwtToken = jwtUtil.getJwtFromHeader(httpServletRequest);
        String ipAddress = ipHelper.getClientIp(httpServletRequest); // 헬퍼 사용

        // 출력
        System.out.println("JWT Token: " + jwtToken);
        System.out.println("IP Address: " + ipAddress);

        if (abusePreventionHelper.isAbusiveRequest(jwtToken, ipAddress)) { // 어뷰징 체크
            System.out.println("어뷰징입니다.");
            return 0;
        } else {
            // 오늘 날짜 가져오기
            LocalDate today = LocalDate.now();
            return videoViewHelper.createOrUpdateHistory(videoId,today); // 헬퍼로 시청 기록 생성
        }
    }

    // 정지(Pause) 메서드
    public void pause(Long userId, Long videoId, int currentPosition) {
        videoViewHelper.updatePlaytime(userId, videoId, currentPosition);
    }

    // 광고 조회수 증가 메서드
    public void adviewcount(AdviewcountRequestDto adviewcountRequestDto) {
        videoViewHelper.incrementViewCount(adviewcountRequestDto.getVideoId()); // 조회수 증가
    }



}