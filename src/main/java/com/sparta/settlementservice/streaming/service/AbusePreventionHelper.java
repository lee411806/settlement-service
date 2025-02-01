package com.sparta.settlementservice.streaming.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AbusePreventionHelper {

    private final RedisTemplate<String, String> redisTemplate;

    // 어뷰징 요청 확인
    public boolean isAbusiveRequest(String jwtToken, String ipAddress) {
        String key = generateKey(jwtToken, ipAddress);

        // Redis에서 키 존재 여부 확인
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            // Redis에 키가 존재하면 어뷰징으로 간주
            System.out.println("어뷰징 요청 감지됨 - Key: " + key);
            return true;
        }

        // Redis에 키가 없으면 새로운 키 추가 및 TTL 설정
        redisTemplate.opsForValue().set(key, "1", 30, TimeUnit.SECONDS);
        System.out.println("새로운 키 생성 및 TTL 설정 완료 - Key: " + key);
        return false;
    }

    // Redis 키 생성
    private String generateKey(String jwtToken, String ipAddress) {
        return jwtToken + ":" + ipAddress;
    }
}