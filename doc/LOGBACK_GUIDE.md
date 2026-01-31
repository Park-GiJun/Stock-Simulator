# 📝 Logback 설정 가이드

> Stock-Simulator 프로젝트의 모든 백엔드 서비스에 적용된 Logback 로깅 설정

## 📋 목차
1. [개요](#개요)
2. [프로파일별 로깅 전략](#프로파일별-로깅-전략)
3. [로그 파일 구조](#로그-파일-구조)
4. [롤링 정책](#롤링-정책)
5. [서비스별 로거 설정](#서비스별-로거-설정)
6. [사용법](#사용법)

---

## 개요

### 적용된 서비스
- ✅ **user-service** - 사용자 인증/관리
- ✅ **stock-service** - 주식 정보/시세
- ✅ **trading-service** - 주문/거래
- ✅ **event-service** - 게임 이벤트
- ✅ **news-service** - 뉴스 생성
- ✅ **scheduler-service** - IPO/Delisting 스케줄링
- ✅ **season-service** - 시즌/랭킹 관리
- ✅ **eureka-server** - 서비스 디스커버리
- ✅ **api-gateway** - API 라우팅

### 핵심 기능
- 🎨 **컬러 로그** (개발 환경)
- 📁 **파일 롤링** (운영 환경)
- 🔍 **추적 ID** (MDC 기반)
- 📊 **로그 레벨 분리** (application/error/api)
- 🔄 **자동 로그 순환** (용량/날짜 기반)

---

## 프로파일별 로깅 전략

### 1. **LOCAL/DEV Profile** (`default`, `dev`)

**특징:**
- ✅ 콘솔 출력만 사용 (파일 없음)
- ✅ 컬러 로그로 가독성 향상
- ✅ SQL 쿼리 로깅 (DEBUG 레벨)
- ✅ 상세한 스택 트레이스

**로그 패턴:**
```
12:34:56.789 [thread-name   ] [traceId  ] DEBUG [service-name] [com.package.ClassName        ] : 메시지
```

**로그 레벨:**
| 카테고리 | 레벨 | 설명 |
|---------|------|------|
| API_LOG | DEBUG | 요청/응답 전체 로깅 |
| Application | DEBUG | 비즈니스 로직 상세 |
| SQL | DEBUG | Hibernate 쿼리 + 파라미터 |
| Spring Framework | INFO | 프레임워크 기본 정보 |
| Kafka | INFO | 메시지 발행/수신 |

---

### 2. **DOCKER Profile**

**특징:**
- ✅ 콘솔 출력 (Docker logs로 확인)
- ✅ 타임스탬프 포함 (날짜 + 시간)
- ✅ 서비스명 포함 (멀티 서비스 구분)
- ❌ SQL 로깅 최소화 (WARN)

**로그 패턴:**
```
2025-01-23 12:34:56.789 [thread-name   ] [traceId  ] INFO  [service-name] [com.package.ClassName        ] : 메시지
```

**로그 레벨:**
| 카테고리 | 레벨 | 설명 |
|---------|------|------|
| API_LOG | INFO | 주요 요청/응답만 |
| Application | INFO | 정상 흐름만 |
| SQL | WARN | 에러만 |
| Spring Framework | WARN | 경고 이상만 |
| Kafka | WARN | 에러만 |

**Docker 로그 확인:**
```bash
# 실시간 로그
docker logs -f stockSimulator-user-service

# 최근 100줄
docker logs --tail 100 stockSimulator-user-service

# 타임스탬프 포함
docker logs -t stockSimulator-user-service

# 특정 시간 이후
docker logs --since "2025-01-23T12:00:00" stockSimulator-user-service
```

---

### 3. **PRODUCTION Profile** (`prd`)

**특징:**
- ✅ 콘솔 + 파일 동시 출력
- ✅ 로그 레벨별 파일 분리 (application/error/api)
- ✅ 자동 롤링 (용량/날짜 기반)
- ✅ 장기 보관 정책
- ❌ 컬러 없음 (파일 저장용)

**로그 패턴:**
```
2025-01-23 12:34:56.789 [thread-name] [traceId---] INFO  [service-name] [com.package.ClassName] : 메시지
```

**로그 레벨:**
| 카테고리 | 레벨 | 설명 |
|---------|------|------|
| API_LOG | DEBUG | 전체 API 로깅 |
| Exception Handler | DEBUG | 예외 상세 |
| Application | INFO | 정상 흐름 |
| SQL | WARN | 에러만 |
| Spring Framework | WARN | 경고 이상만 |

---

## 로그 파일 구조

### 운영 환경 (`prd`) 파일 구조

```
logs/
├── user-service/
│   ├── application.log           # 전체 로그
│   ├── application-2025-01-23.0.log  # 롤링된 과거 로그
│   ├── error.log                 # WARN/ERROR만
│   ├── error-2025-01-23.0.log
│   └── api.log                   # API 요청/응답만
├── stock-service/
│   ├── application.log
│   ├── error.log
│   └── api.log
├── trading-service/
│   └── ...
└── api-gateway/
    └── ...
```

### 로그 파일 종류

| 파일명 | 내용 | 크기 제한 | 보관 기간 |
|--------|------|----------|----------|
| **application.log** | 전체 애플리케이션 로그 | 50MB/파일 | 30일 (총 500MB) |
| **error.log** | WARN/ERROR만 | 50MB/파일 | 60일 (총 300MB) |
| **api.log** | API 요청/응답만 | 50MB/파일 | 30일 (총 500MB) |

---

## 롤링 정책

### 1. **SizeAndTimeBasedRollingPolicy**

**작동 방식:**
```xml
<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>logs/${SERVICE_NAME}/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <maxFileSize>50MB</maxFileSize>           <!-- 파일당 최대 크기 -->
    <totalSizeCap>500MB</totalSizeCap>        <!-- 전체 최대 크기 -->
    <maxHistory>30</maxHistory>                <!-- 최대 보관 일수 -->
</rollingPolicy>
```

**롤링 조건:**
1. **파일 크기**: 50MB 초과 시 새 파일 생성 (`%i` 증가)
2. **날짜**: 자정 넘어가면 새 날짜 파일 생성
3. **전체 크기**: 500MB 초과 시 오래된 파일 삭제
4. **보관 기간**: 30일 초과 파일 자동 삭제

**예시:**
```
application-2025-01-23.0.log  (50MB - 가득 참)
application-2025-01-23.1.log  (30MB - 현재 작성 중)
application-2025-01-22.0.log  (50MB)
application-2025-01-21.0.log  (50MB)
... (30일치 보관)
```

### 2. **파일별 롤링 정책 차이**

| 로그 파일 | maxFileSize | totalSizeCap | maxHistory |
|----------|-------------|--------------|------------|
| application.log | 50MB | 500MB | 30일 |
| error.log | 50MB | 300MB | 60일 (에러는 오래 보관) |
| api.log | 50MB | 500MB | 30일 |

---

## 서비스별 로거 설정

### User Service

```xml
<!-- API 로깅 -->
<logger name="API_LOG" level="DEBUG"/>

<!-- 애플리케이션 로그 -->
<logger name="com.stocksimulator.userservice" level="DEBUG"/>
<logger name="com.stocksimulator.common" level="DEBUG"/>

<!-- Spring Security -->
<logger name="org.springframework.security" level="DEBUG"/>

<!-- SQL 로깅 -->
<logger name="org.hibernate.SQL" level="DEBUG"/>
<logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>
```

### Stock Service / Trading Service

```xml
<!-- Redis 로깅 추가 -->
<logger name="org.redisson" level="INFO"/>

<!-- Kotlin JDSL -->
<logger name="com.linecorp.kotlinjdsl" level="DEBUG"/>
```

### Event Service / News Service

```xml
<!-- MongoDB 로깅 -->
<logger name="org.springframework.data.mongodb" level="DEBUG"/>

<!-- Elasticsearch (News만) -->
<logger name="org.elasticsearch" level="INFO"/>
```

### Scheduler Service

```xml
<!-- 스케줄링 로깅 -->
<logger name="org.springframework.scheduling" level="DEBUG"/>
```

### API Gateway

```xml
<!-- Gateway 라우팅 로깅 -->
<logger name="org.springframework.cloud.gateway" level="DEBUG"/>

<!-- Netty 로깅 -->
<logger name="reactor.netty" level="INFO"/>
```

---

## 사용법

### 1. **프로파일별 실행**

#### 로컬 개발 (컬러 로그)
```bash
# application.yml의 기본 프로파일 사용
./gradlew :backend:user-service:bootRun

# 또는 명시적으로 dev 프로파일
./gradlew :backend:user-service:bootRun --args='--spring.profiles.active=dev'
```

#### Docker 환경
```bash
# docker-compose.yml에서 SPRING_PROFILES_ACTIVE=docker 설정됨
docker-compose --profile all up -d

# 로그 확인
docker logs -f stockSimulator-user-service
```

#### 운영 환경 (파일 로깅)
```bash
# JAR 실행 시
java -jar user-service.jar --spring.profiles.active=prd

# 로그 파일 확인
tail -f logs/user-service/application.log
tail -f logs/user-service/error.log
```

---

### 2. **코드에서 로거 사용**

#### Kotlin 코드 예시

```kotlin
package com.stocksimulator.userservice.application.service

import org.slf4j.LoggerFactory

class UserService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    fun signUp(request: SignUpRequest): User {
        logger.info("회원가입 시작: email={}", request.email)
        
        try {
            val user = userRepository.save(...)
            logger.debug("사용자 저장 완료: userId={}", user.userId)
            return user
        } catch (e: Exception) {
            logger.error("회원가입 실패: email={}", request.email, e)
            throw e
        }
    }
}
```

#### API 로그 (컨트롤러)

```kotlin
package com.stocksimulator.userservice.adapter.`in`.web.controller

import org.slf4j.LoggerFactory

@RestController
class AuthController {
    private val apiLogger = LoggerFactory.getLogger("API_LOG")
    
    @PostMapping("/api/v1/auth/signup")
    suspend fun signUp(@RequestBody request: SignUpRequest): ApiResponse<UserResponse> {
        apiLogger.info("POST /api/v1/auth/signup - request: {}", request)
        
        val user = authService.signUp(request)
        val response = ApiResponse.success(user.toResponse())
        
        apiLogger.info("POST /api/v1/auth/signup - response: {}", response)
        return response
    }
}
```

---

### 3. **MDC (추적 ID) 사용**

#### WebFilter로 자동 추가

```kotlin
package com.stocksimulator.common.filter

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class TraceIdFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val traceId = UUID.randomUUID().toString().substring(0, 8)
        MDC.put("traceId", traceId)
        
        return chain.filter(exchange)
            .doFinally { MDC.clear() }
    }
}
```

#### 로그에 traceId 자동 포함

```
12:34:56.789 [reactor-1] [a3f8d91c] INFO  [user-service] [AuthController] : POST /api/v1/auth/signup
12:34:56.790 [reactor-1] [a3f8d91c] DEBUG [user-service] [UserService] : 사용자 저장 완료
```

---

### 4. **로그 확인 명령어**

#### 실시간 로그 모니터링

```bash
# 전체 로그
tail -f logs/user-service/application.log

# 에러 로그만
tail -f logs/user-service/error.log

# API 로그만
tail -f logs/user-service/api.log

# 여러 파일 동시 모니터링
tail -f logs/*/application.log
```

#### 로그 검색

```bash
# 특정 키워드 검색
grep "회원가입" logs/user-service/application.log

# 에러만 추출
grep "ERROR" logs/user-service/application.log

# 특정 traceId 추적
grep "a3f8d91c" logs/user-service/application.log

# 날짜별 검색
grep "2025-01-23 12:" logs/user-service/application.log
```

#### 로그 용량 확인

```bash
# 서비스별 용량
du -sh logs/*

# 전체 용량
du -sh logs

# 파일 개수
find logs -name "*.log" | wc -l
```

---

### 5. **로그 레벨 런타임 변경**

#### Actuator Endpoint 사용

```bash
# 현재 로그 레벨 확인
curl http://localhost:8081/actuator/loggers/com.stocksimulator.userservice

# 로그 레벨 변경 (재시작 없이)
curl -X POST http://localhost:8081/actuator/loggers/com.stocksimulator.userservice \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# SQL 로깅 활성화
curl -X POST http://localhost:8081/actuator/loggers/org.hibernate.SQL \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 트러블슈팅

### 1. 로그 파일이 생성되지 않음

**원인:** `prd` 프로파일이 활성화되지 않음

**해결:**
```bash
# 프로파일 확인
grep "active:" application.yml

# 명시적으로 prd 프로파일 지정
java -jar app.jar --spring.profiles.active=prd
```

---

### 2. 로그 파일이 롤링되지 않음

**원인:** 파일 크기가 `maxFileSize` 미만

**확인:**
```bash
ls -lh logs/user-service/
```

**해결:** 대기하거나 강제로 로그 생성

---

### 3. 로그가 너무 많이 쌓임

**원인:** DEBUG 레벨로 운영 중

**해결:**
```xml
<!-- prd 프로파일에서 INFO 레벨로 변경 -->
<logger name="com.stocksimulator.userservice" level="INFO"/>
```

---

### 4. Docker에서 로그가 안 보임

**확인:**
```bash
# 컨테이너 상태 확인
docker ps

# 로그 확인
docker logs stockSimulator-user-service

# 프로파일 확인
docker exec stockSimulator-user-service env | grep SPRING_PROFILES_ACTIVE
```

---

## 베스트 프랙티스

### 1. **로그 레벨 가이드**

| 레벨 | 사용 시점 | 예시 |
|------|----------|------|
| **TRACE** | 매우 상세한 디버깅 | SQL 파라미터 바인딩 |
| **DEBUG** | 개발 시 디버깅 | 메서드 진입/종료, 변수값 |
| **INFO** | 정상 흐름 | API 요청/응답, 비즈니스 이벤트 |
| **WARN** | 잠재적 문제 | 재시도, Fallback, Deprecated |
| **ERROR** | 예외 발생 | Exception, 비즈니스 에러 |

### 2. **로그 메시지 작성 규칙**

```kotlin
// ❌ 나쁜 예
logger.info("error")  // 의미 불명확
logger.error("User signup failed")  // 컨텍스트 부족

// ✅ 좋은 예
logger.info("회원가입 시작: email={}, username={}", request.email, request.username)
logger.error("회원가입 실패: email={}, reason={}", request.email, e.message, e)
```

### 3. **민감 정보 로깅 금지**

```kotlin
// ❌ 절대 금지!
logger.debug("비밀번호: {}", request.password)
logger.info("카드 번호: {}", cardNumber)

// ✅ 마스킹 처리
logger.debug("비밀번호: ******")
logger.info("카드 번호: ****-****-****-{}", cardNumber.takeLast(4))
```

---

## 참고 자료

- [Logback 공식 문서](https://logback.qos.ch/manual/index.html)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [SLF4J 문서](http://www.slf4j.org/manual.html)

---

**문서 버전:** v1.0  
**작성일:** 2025-01-23  
**적용 서비스:** 전체 백엔드 서비스 (9개)
