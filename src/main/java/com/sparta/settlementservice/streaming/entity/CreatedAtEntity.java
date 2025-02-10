package com.sparta.settlementservice.streaming.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass  // 상속받는 엔티티에 필드를 추가
@EntityListeners(AuditingEntityListener.class)  // 자동으로 생성/수정 시간 기록
@Getter
public class CreatedAtEntity  {

    @CreatedDate  // 생성 시간 자동 기록
    @Column(updatable = false)  // 수정 불가
    private LocalDateTime createdAt;

}