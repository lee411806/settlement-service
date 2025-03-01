package com.sparta.settlementservice.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@ConfigurationProperties(prefix = "jwt")  // "jwt.secret" 값을 자동으로 매핑
@Getter
@Setter  //  yml 값을 주입받기 위해 Setter 필요
public class JWTUtil {

    private String secret;  //  yml에서 주입될 필드
    private SecretKey secretKey;
    private static final String COOKIE_NAME = "Authorization";

    @PostConstruct
    public void init() {  //  yml 값이 설정된 이후 실행되는 초기화 메서드
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        System.out.println("JWT SECRET LOADED: " + secret);  // 확인용 로그
    }

    //  쿠키에서 JWT 가져오는 메서드
    public String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue(); // 🔥 JWT 반환
                }
            }
        }
        return null; // JWT 쿠키가 없으면 null 반환
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 토큰 만료뿐만아니라 서명 검증도 들어가있음
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
