# 정산 시스템 프로젝트

📅 <b>2024.11.18 ~ 2025.06.12 </b> | (1명)

[![Java][Java]][Java-url]
[![Spring Boot][SpringBoot]][SpringBoot-url]
[![Spring Batch][SpringBatch]][SpringBatch-url]
[![Spring Security][SpringSecurity]][SpringSecurity-url]
[![JWT][JWT]][JWT-url]
[![JPA][JPA]][JPA-url]
[![MySQL][MySQL]][MySQL-url]
[![Redis][Redis]][Redis-url]
[![MasterSlave][MasterSlave]][MasterSlave-url]
[![JMeter][JMeter]][JMeter-url]
[![GitHub Actions][GitHubActions]][GitHubActions-url]
[![Docker Compose][DockerCompose]][DockerCompose-url]

<!-- Badge 이미지 링크 -->
[Java]: https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white
[SpringBoot]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white
[SpringBatch]: https://img.shields.io/badge/Spring%20Batch-4DC71F?style=for-the-badge&logo=spring&logoColor=white
[SpringSecurity]: https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white
[JPA]: https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white
[MySQL]: https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white
[Redis]: https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white
[JWT]: https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white  
[MasterSlave]: https://img.shields.io/badge/Master--Slave-555555?style=for-the-badge&logo=databricks&logoColor=white  
[JMeter]: https://img.shields.io/badge/JMeter-D22128?style=for-the-badge&logo=apache-jmeter&logoColor=white  
[GitHubActions]: https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white
[DockerCompose]: https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white

<!-- 웹사이트 링크 -->
[Java-url]: https://www.oracle.com/java/
[SpringBoot-url]: https://spring.io/projects/spring-boot
[SpringBatch-url]: https://spring.io/projects/spring-batch
[SpringSecurity-url]: https://spring.io/projects/spring-security
[JPA-url]: https://spring.io/projects/spring-data-jpa
[MySQL-url]: https://www.mysql.com/
[Redis-url]: https://redis.io/
[JWT-url]: https://jwt.io/  
[MasterSlave-url]: https://en.wikipedia.org/wiki/Master/slave_(technology)  
[JMeter-url]: https://jmeter.apache.org/
[GitHubActions-url]: https://github.com/features/actions
[DockerCompose-url]: https://docs.docker.com/compose/

## 기능
| **User-service**          |    **User-Streaming-service**       |
|---------------------|-------------------------------------------------------------|
|   회원가입       |              동영상 통계 조회 : 일간/주간/월간 (조회수, 재생시간)    |
|   로그인   |  동영상 정산 조회 : 일간/주간/월간  |
|   로그아웃   |  어뷰징 방지 |

<br>



## 배경

- 실시간으로 유입되는 데이터가 많아질 경우, **매 요청마다 하나의 RDBMS 처리·조회하면 DB 서버에 부하가 누적**되어 한계에 도달할 수 있다고 판단  
- 최적화 없이 RDBMS 서버 부하가 증가할 경우, **사용자 대기 시간이 늘어나고 불필요한 리소스 사용으로 인프라 비용이 상승**  
- **대용량 데이터에 대한 효율적인 처리 방안**을 마련해야 할 필요성 인식  



<br>

## 해결 방법

1. 대량 데이터 **Batch로 일괄처리**
    - **Batch 4단계 최적화**를 통해 조회 전용 테이블 데이터 입력, RDBMS 부하 최소화
    - 로그데이터 **3200만 건 기준 2분 12초 처리 (99.83% 성능 향상)**
2. **Read Replica 활용**
    - Replica를 활용해 **생성, 조회 DB를 분리**, 읽기 요청을 분산하여 처리하여 부하 완화
3. **Redis 캐싱**을 통한 실시간 응답 속도 개선
    - 어뷰징 방지 기능 API: **100만 건 기준 283ms → 14ms (95.05% 향상)**
4. **JMeter 부하 테스트**를 통한 API 성능 분석
    - **TPS(초당 처리량), P99 응답시간** 등 핵심 지표 모니터링
    - 성능 데이터 기반으로 병목 구간 식별 및 개선 적용
  
<br>


## 🔥프로젝트 경험

<h2 align="center"> 1. 배치 작업 성능 개선 (99.83% 향상)</h2>

### 1.1 최종 성능
- 3200만 개 데이터 기준 처리 결과: **2분 12초 (2m12s)**


### 1.2 성능 개선 추이
| **단계**          | **데이터 규모**     | **처리 시간**         | **개선율**       |
|--------------------|---------------------|-----------------------|------------------|
| **최적화 전**      | 3200만 건           | 약 80,000초 (22시간 13분) (추정) | -                |
| **4차 최적화**     | 3200만 건           | 약 132초 (2분 12초)      | 약 99.83%        |


### 1.3 주요 개선 포인트 
- **1차 최적화**: 데이터베이스 인덱싱 추가, Chunk 크기 조정 데이터  
- **2차 최적화**: 파티셔닝 멀티스레드 도입
- **3차 최적화**: 스케일 업 (MySQL 버퍼 풀 크기 조정)
- **4차 최적화**: JPA 제거 후 JDBC 사용, 벌크 연산 도입

<br>

> 🔗 **성능 개선 과정 전체 정리는 [여기](https://www.notion.so/1e943673dd8c806484f6c33a64ad959d)에서 확인할 수 있습니다.**

<br>
<h2 align="center"> 2. 조회 성능 향상</h2>

### 2.1 Primary Replica 도입
 -** GTID 기반 Replica** 생성 (생성 DB commit 시 Replica 동기화)
 - 읽기 요청(GET)을 Replica로 분리 처리

### 2.2 트랜잭션 및 커넥션 설정
   - POST API **트랜잭션 최소화 (Atomic transaction**)
   - **OSIV 비활성화** → 요청 종료 시점이 아닌 즉시 커넥션 반환
   - Connection idle-timeout: 60분 → 30초로 단축하여 유휴 커넥션 회수
   - 각 DB 서버 **커넥션 풀: 10개 → 30개로 확장**

### 2.2 JMeter 부하 테스트 결과
- 조회 요청 10,000건 기준  적용 전 대비 최대 1634배 향상, TPS 18.3배 증가
- 쓰기(POST)와 조회(GET) 요청을 동시에 실행하여 테스트
- 성능 개선 추이


  
  | **지표**       | **적용 전** | **적용 후** | **개선 내용**                           |
  |----------------|-------------|-------------|------------------------------------------|
  | **90% 지점**    | 354ms       | 27ms        |  **327ms 개선 (13.1배 향상)**           |
  | **95% 지점**    | 610ms       | 34ms        |  **576ms 개선 (17.9배 향상)**           |
  | **99% 지점**    | 84994ms     | 52ms        |  **84942ms 개선 (1634배 향상)**        |
  | **Throughput** | 28 TPS      | 513 TPS     |  **18.3배 증가**                        |

<sub>🛈 GET 요청은 정상 처리되었으나, POST와 커넥션 풀을 공유해 일부 요청이 지연되었습니다.  
JMeter는 응답 도착 기준으로 수치를 계산하므로, 종료 시점에 수치가 비정상적으로 튈 수 있습니다.  
→ 이후 결과를 재검토했을 때, 성능 개선 수치는 실제 체감 성능과 불일치하였으며, 부하 종료 타이밍 외에도 **성능 개선 전 수치 자체가 비정상적으로 높아 정확성이 떨어질 가능성이 높았습니다.**  본 수치는 당시 병목 해결 과정을 기록하기 위한 참고용으로만 보관하며, MSA 전환 프로젝트에서의 병목 해결 사례가 있습니다.</sub>




<br> 

> 🔗 **성능 개선 과정 전체 정리는 [여기](https://www.notion.so/1e343673dd8c80c29a57dd97915920a7)에서 확인할 수 있습니다.**


<br>

<h2 align="center"> 3. 어뷰징 방지 기능 최적화(95.05% 향상) </h2>

### 3.1 Redis
  - 100만개 데이터 기준 처리 결과 : 14ms (20번 요청 평균)
    
  - 성능 개선 지표

    | **단계**          | **데이터 규모**     | **처리 시간**         | **개선율**       |
    |--------------------|---------------------|-----------------------|------------------|
    | **최적화 전**      | 100만 건           | 약 283ms              | -                |
    | **최적화 후**      | 100만 건           | 약 14ms               | 약 95.05%        |


 - 주요 개선 포인트
   - RDBMS의 복잡한 읽기/쓰기 과정을 Redis 캐시로 대체
   - 30초 만료 기능(TTL)을 활용한 데이터 자동 삭제로 검증요청 간소화

<br>

## 🔫 트러블 슈팅
- [Spring Batch 순환 참조 및 StepScope 병렬 처리 문제](https://www.notion.so/Spring-Batch-StepScope-20143673dd8c807ea829e1523356c396)
- [Chunk 단위 배치에서 데이터 누적 저장 문제](https://www.notion.so/Chunk-20143673dd8c80d29224ee27a3d574b7)
- [GTID 복제 문제](https://www.notion.so/Slave-1dd43673dd8c804ea65bf12ac33294c4)
- [Spring Boot 멀티 DB 설정 시 Repository 중복 등록 문제](https://www.notion.so/Spring-Boot-DB-Repository-1de43673dd8c80e998ede0db785e4e8b)
- [Dockerfile 누락 + 환경 분리 + 포트 충돌](https://www.notion.so/Docker-compose-1dd43673dd8c8041915dd214914ee35c)
- [📚그 외 트러블슈팅 모음](https://www.notion.so/1dd43673dd8c80d9a894d5ab991b6952)

<br>

## 🔎기술적 의사결정
- [GitHub Actions 사전 검증을 위한 act 도입](https://www.notion.so/GitHub-Actions-act-1dd43673dd8c80379e69f1a008578c23)
- [Replica 복제 방식 의사결정](https://www.notion.so/Replica-GTID-1e243673dd8c8024b883edc527019ccf)
- [낙관적, 비관적 락 + Atomic Update 적용 의사결정](https://www.notion.so/Atomic-Update-1f243673dd8c80188185edea0290c6f4)
- [Top5 & 정산 데이터 테이블 설계 의사결정](https://www.notion.so/Top5-20143673dd8c802fb3fbd45066f48948)
- [📚그 외 의사결정 모음](https://www.notion.so/1dd43673dd8c8085806ad7d111e7fa9a)


<br>



## 🏗 아키텍쳐
<img width="593" alt="화면 캡처 2025-04-05 195228" src="https://github.com/user-attachments/assets/6ab565d3-332d-4957-a0db-100fd0c49e1b" />



<br>

 
## :bookmark: API 문서

[![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)](https://documenter.getpostman.com/view/30989395/2sAYBPktii)[![API Documentation](https://img.shields.io/badge/API%20Documentation-6B7280?style=for-the-badge&logo=book&logoColor=white)](https://documenter.getpostman.com/view/30989395/2sAYBPktii)

<br>


## 📙ERD
<img width="800" alt="화면 캡처 2025-04-05 195107" src="https://github.com/user-attachments/assets/027814ec-5721-420f-bc5c-7914d166bad8" />





