package com.sparta.settlementservice.repository;


import com.sparta.settlementservice.entity.ReviewCountAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCountAuthenticationRepository extends JpaRepository<ReviewCountAuthentication, Long> {


    Optional<ReviewCountAuthentication> findByJwtTokenAndIpAddress(String jwtToken, String ipAddress);
}
