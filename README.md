# 정산 시스템 프로젝트

📅 <b>2024.11 ~ 2024.12 (4주)</b> | (1명)

[![Java][Java]][Java-url]
[![Spring Boot][SpringBoot]][SpringBoot-url]
[![Spring Batch][SpringBatch]][SpringBatch-url]
[![Spring Security][SpringSecurity]][SpringSecurity-url]
[![JPA][JPA]][JPA-url]
[![MySQL][MySQL]][MySQL-url]
[![Redis][Redis]][Redis-url]

<!-- Badge 이미지 링크 -->
[Java]: https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white
[SpringBoot]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white
[SpringBatch]: https://img.shields.io/badge/Spring%20Batch-4DC71F?style=for-the-badge&logo=spring&logoColor=white
[SpringSecurity]: https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white
[JPA]: https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white
[MySQL]: https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white
[Redis]: https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white

<!-- 웹사이트 링크 -->
[Java-url]: https://www.oracle.com/java/
[SpringBoot-url]: https://spring.io/projects/spring-boot
[SpringBatch-url]: https://spring.io/projects/spring-batch
[SpringSecurity-url]: https://spring.io/projects/spring-security
[JPA-url]: https://spring.io/projects/spring-data-jpa
[MySQL-url]: https://www.mysql.com/
[Redis-url]: https://redis.io/



### 기능
| **User-service**          |    **User-Streaming-service**       |
|---------------------|-------------------------------------------------------------|
|   회원가입       |              동영상 통계 조회 : 일간/주간/월간 (조회수, 재생시간)    |
|   로그인   |  동영상 정산 조회 : 일간/주간/월간  |
|   로그아웃   |  어뷰징 방지 |



## 프로젝트 목표
1. **단일 서버에서 대규모 데이터를 최적으로 처리할 수 있는 배치 시스템 구현**  <br>
2. **Redis를 활용한 실시간 데이터 처리**


<br>


## 🔥프로젝트 경험

## 1. 배치 초당 처리량 성능 개선 (60,506% 향상)

### 1.1 최종 성능
- 3200만 개 데이터 기준 처리 결과: **2분 12초 (2m12s)**
- 기존 대비 **60,506% 성능 향상**.


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

### 1.4 최종 결론
- 5000만 건 이상 데이터 처리 시 메모리 부족(OutOfMemory) 문제가 발생할 가능성 높음
  - 데이터를 3200만 건 이하로 조정
  - 분산 처리 환경 또는 배치 클러스터 구성이 필요


## 🔫 트러블 슈팅
- **대용량 데이터 처리 문제**  
  - 대량 요청이 서버에 실시간으로 들어올 경우 **메모리 부족(OutOfMemoryError)** 및 **성능 저하** 문제를 방지하기 위해 **배치 프로그램을 도입**

- **멀티스레드 적용 문제**  
  - **청크 단위에서 멀티스레드 처리**를 적용해도 순차적으로 처리되는 한계가 있어, 데이터를 **파티셔닝**으로 분리하고 각 파티션을 **비동기로 병렬 처리**하여 **성능**과 **확장성**을 개선

- **배치 코드 복잡성 문제**  
  - 주/월 단위 배치를 하나의 코드로 처리하려다 불필요한 복잡도가 발생하여, 배치를 각각 분리함으로써 **코드 간결화**, **관리 용이성**, **확장성**을 개선

- **동시성 문제**  
  - 조회수 증가 시 **비관적 락**으로 데이터 충돌 문제를 방지하고, 멀티스레드 구현 시 **videoId**를 기준으로 파티셔닝하여 충돌 가능성이 낮다고 판단, **낙관적 락**을 적용하여 **성능**과 **안정성**을 확보


기술적 의사결정은 문제를 예방하고 효율적인 시스템을 설계하는 과정이고, 트러블 슈팅은 이미 발생한 문제를 해결하는 대응 과정 위 트러블 슈팅은 적절한가요?

<br>




## 🏗 아키텍쳐
![image](https://github.com/user-attachments/assets/b6f8c531-9f69-4083-8d1d-b26b59112463)

<br>

## 🗂 폴더구조
```
┣ 📁streaming-service
     ┣ 📁batch
        ┣ 📁controller
        ┣ 📁entity
        ┣ 📁repo
        ┣ 📁schedule
        ┣ 📁settlementbatch
     ┣ 📁controller
     ┣ 📁entity
     ┣ 📁jwt
     ┣ 📁repository
     ┣ 📁service
┣ 📁user-service
     ┣ 📁java/com/sparta/userservice
        ┣ 📁config
        ┣ 📁controller
        ┣ 📁dto
        ┣ 📁jwt
        ┣ 📁security
        ┣ 📁service
     ┣ 📁resources
        ┣ 📁static
        ┣ 📁templates
┣ 📁settlement-service
```

<br>

## :bookmark: API 문서
🔗 [Postman API Documentation](https://documenter.getpostman.com/view/30989395/2sAYBPktii)

<br>


## 프로젝트 라이선스

- 이 프로젝트는 [MIT License](./LICENSE) 하에 배포됩니다.
- 자세한 내용은 `LICENSE` 파일을 참조하세요.

<br>


## 🛠️ 지원 창구

### 연락 방법
- 이메일: jaeyonglee06@gmail.com
- GitHub Issues: https://github.com/lee411806/settlement-platform/issues

### 문제 보고
- 🐞 버그 신고: [버그 신고 템플릿 바로가기](https://github.com/lee411806/settlement-platform/issues/new?assignees=&labels=&projects=&template=%F0%9F%90%9E-%EB%B2%84%EA%B7%B8-%EC%8B%A0%EA%B3%A0.md&title=%22%5BBUG%5D+%3C%EB%B2%84%EA%B7%B8+%EC%9A%94%EC%95%BD%3E%22)

### 문서 및 가이드
- 공식 문서: 설치 가이드, api 사용법 넣을 예정
- FAQ: 자주 발생한 에러 넣을 예정

### 지원 정책
- 지원 시간: 평일 오전 9시 ~ 오후 6시 (KST)
- 긴급 문의: jaeyonglee06@gmail.com으로 연락해주세요.
