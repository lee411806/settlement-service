# 정산 시스템 프로젝트

📅 <b>2024.11 ~ 2024.12 (4주) , 보완 중 </b> | (1명)

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

### 기능
| **User-service**          |    **User-Streaming-service**       |
|---------------------|-------------------------------------------------------------|
|   회원가입       |              동영상 통계 조회 : 일간/주간/월간 (조회수, 재생시간)    |
|   로그인   |  동영상 정산 조회 : 일간/주간/월간  |
|   로그아웃   |  어뷰징 방지 |



## 프로젝트 목표
1. **단일 서버에서 대규모 데이터를 최적으로 처리할 수 있는 시스템 구현**  <br>
2. **실시간 데이터 처리**
3. **가용성 확보**
4. **성능 부하 테스트 및 병목 구간 확인**


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
<h2 align="center"> 2. 조회 성능 및 가용성 향상</h2>



### 2.1 JMeter 부하 테스트 결과
- 조회 요청 10,000건 기준  적용 전 대비 최대 **1634배 향상**, TPS **18.3배 증가**
- 쓰기(POST)와 조회(GET) 요청을 동시에 실행하여 테스트
- 성능 개선 추이
  | **지표**       | **적용 전** | **적용 후** | **개선 내용**                           |
  |----------------|-------------|-------------|------------------------------------------|
  | **90% 지점**    | 354ms       | 27ms        |  **327ms 개선 (13.1배 향상)**           |
  | **95% 지점**    | 610ms       | 34ms        |  **576ms 개선 (17.9배 향상)**           |
  | **99% 지점**    | 84994ms     | 52ms        |  **84942ms 개선 (1634배 향상)**        |
  | **Throughput** | 28 TPS      | 513 TPS     |  **18.3배 증가**                        |

### 2.2 Master Slave 
- GTID 기반 Replica 생성
- 읽기 요청을 slave서버로 보내 커넥션 수 분산 및 쓰기 조회 책임 분리

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





