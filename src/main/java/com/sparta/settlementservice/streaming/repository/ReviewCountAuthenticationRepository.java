package com.sparta.settlementservice.streaming.repository;


import com.sparta.settlementservice.streaming.entity.ReviewCountAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCountAuthenticationRepository extends JpaRepository<ReviewCountAuthentication, Long> {


    Optional<ReviewCountAuthentication> findByJwtTokenAndIpAddress(String jwtToken, String ipAddress);
}
