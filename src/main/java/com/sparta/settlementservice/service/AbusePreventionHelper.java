package com.sparta.settlementservice.service;

import com.sparta.settlementservice.entity.ReviewCountAuthentication;
import com.sparta.settlementservice.repository.ReviewCountAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AbusePreventionHelper {

    private final ReviewCountAuthenticationRepository reviewCountAuthenticationRepository;

    // 어뷰징 요청 확인
    public boolean isAbusiveRequest(String jwtToken, String ipAddress) {
        Optional<ReviewCountAuthentication> reviewCountAuthentication = reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress);

        int currentTimeInSeconds = (int) (System.currentTimeMillis() / 1000);

        if (reviewCountAuthentication.isPresent()) {
            ReviewCountAuthentication authLog = reviewCountAuthentication.get();
            boolean isAbusive = (currentTimeInSeconds - authLog.getLastActionTime()) <= 30;

            if (isAbusive) {
                updateLastActionTime(authLog, currentTimeInSeconds);
                return true;
            }

            return false;
        } else {
            createInitialReviewCountAuthentication(jwtToken, ipAddress, currentTimeInSeconds);
            return false;
        }
    }

    // 어뷰징 기록 생성
    private void createInitialReviewCountAuthentication(String jwtToken, String ipAddress, int currentTimeInSeconds) {
        ReviewCountAuthentication newAuthLog = new ReviewCountAuthentication();
        newAuthLog.setJwtToken(jwtToken);
        newAuthLog.setIpAddress(ipAddress);
        newAuthLog.setLastActionTime(currentTimeInSeconds);
        reviewCountAuthenticationRepository.save(newAuthLog);
        System.out.println("어뷰징 방지 기록 새로 생성 및 저장 완료");
    }

    // lastActionTime 업데이트
    private void updateLastActionTime(ReviewCountAuthentication authLog, int currentTimeInSeconds) {
        authLog.setLastActionTime(currentTimeInSeconds);
        reviewCountAuthenticationRepository.save(authLog);
        System.out.println("어뷰징 요청 감지됨 - lastActionTime 업데이트 완료");
    }
}