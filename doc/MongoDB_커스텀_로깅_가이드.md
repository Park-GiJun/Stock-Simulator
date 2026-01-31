# MongoDB 커스텀 로깅 시스템 가이드

## 📚 목차
1. [개요](#개요)
2. [설정 방법](#설정-방법)
3. [사용 방법](#사용-방법)
4. [MongoDB 쿼리 예시](#mongodb-쿼리-예시)
5. [인덱스 생성](#인덱스-생성)
6. [트러블슈팅](#트러블슈팅)

---

## 개요

모든 마이크로서비스의 로그를 MongoDB에 중앙 집중식으로 저장하는 시스템입니다.

### 주요 기능
- ✅ **모든 서비스 로그 통합**: user-service, stock-service, api-gateway 등 모든 로그 통합
- ✅ **구조화된 로깅**: 메타데이터(userId, orderId 등)를 함께 저장
- ✅ **TraceId 추적**: 분산 트레이싱을 통한 요청 전체 추적
- ✅ **비동기 처리**: 애플리케이션 성능에 영향 없음
- ✅ **API Gateway 자동 로깅**: 모든 HTTP 요청/응답 자동 기록
- ✅ **Eureka 이벤트 로깅**: 서비스 등록/해제 자동 기록

### 저장 구조

```javascript
{
  "_id": "abc123...",
  "timestamp": ISODate("2025-01-15T10:30:45.123Z"),
  "serviceName": "user-service",
  "level": "INFO",
  "traceId": "a1b2c3d4",
  "threadName": "http-nio-8081-exec-1",
  "logger": "UserController",
  "message": "User registered successfully",
  "metadata": {
    "userId": 12345,
    "email": "user@example.com",
    "ipAddress": "192.168.1.100"
  },
  "method": "POST",             // API 로그 전용
  "path": "/api/users/register", // API 로그 전용
  "statusCode": 200,             // API 로그 전용
  "duration": 156                // API 로그 전용 (ms)
}
```

---

## 설정 방법

### 1. Common 모듈 의존성 확인

`backend/common/build.gradle.kts`에 이미 추가되어 있습니다:

```kotlin
// MongoDB for custom logging
implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

// Logback for structured logging
implementation("net.logstash.logback:logstash-logback-encoder:7.4")
```

### 2. 각 서비스 설정 파일 수정

#### `application.yml` (로컬 개발용)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://stocksim:stocksim123@localhost:27017/stocksimulator?authSource=admin

logging:
  mongodb:
    enabled: true  # MongoDB 로깅 활성화
```

#### `application-docker.yml` (Docker 환경용)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USER}:${MONGO_PASSWORD}@${MONGO_HOST}:27017/stocksimulator?authSource=admin

logging:
  mongodb:
    enabled: ${LOGGING_MONGODB_ENABLED:true}
```

### 3. 환경 변수 (.env 파일)

```bash
# MongoDB 설정 (이미 존재)
MONGO_HOST=172.30.1.79
MONGO_USER=stocksim
MONGO_PASSWORD=stocksim123

# MongoDB 로깅 활성화 (선택적)
LOGGING_MONGODB_ENABLED=true
```

---

## 사용 방법

### 1. 기본 로깅

```kotlin
import com.stocksimulator.common.logging.CustomLogger

class UserController {
    private val log = CustomLogger(UserController::class.java)
    
    fun registerUser(request: RegisterRequest): User {
        val user = userService.register(request)
        
        // 메타데이터와 함께 로깅
        log.info("User registered", mapOf(
            "userId" to user.id,
            "email" to user.email,
            "ipAddress" to getClientIp()
        ))
        
        return user
    }
}
```

### 2. 에러 로깅

```kotlin
try {
    // 비즈니스 로직
    orderService.createOrder(order)
} catch (e: InsufficientBalanceException) {
    log.error("Order creation failed", e, mapOf(
        "userId" to userId,
        "orderId" to orderId,
        "requiredAmount" to order.amount,
        "availableBalance" to user.balance
    ))
    throw e
}
```

### 3. API 로깅 (API Gateway에서 자동)

API Gateway의 `RequestResponseLoggingFilter`가 자동으로 처리합니다.

```kotlin
// 자동으로 기록됨:
// GET /user-service/api/users/123 -> 200 (45ms)
// POST /stock-service/api/orders -> 201 (123ms)
```

### 4. 다양한 로그 레벨

```kotlin
log.trace("Very detailed debug info", mapOf("key" to "value"))
log.debug("Debug information", mapOf("step" to 1))
log.info("Normal operation", mapOf("action" to "completed"))
log.warn("Warning message", null, mapOf("warning" to "deprecated"))
log.error("Error occurred", exception, mapOf("context" to "payment"))
```

### 5. TraceId 설정 (필터/인터셉터)

```kotlin
import com.stocksimulator.common.logging.CustomLogger

class CustomFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val traceId = UUID.randomUUID().toString().substring(0, 8)
        CustomLogger.setTraceId(traceId)
        
        try {
            chain.doFilter(request, response)
        } finally {
            CustomLogger.clearTraceId()
        }
    }
}
```

---

## MongoDB 쿼리 예시

### 1. 최근 에러 로그 조회

```javascript
db.application_logs.find({ 
  level: "ERROR" 
})
.sort({ timestamp: -1 })
.limit(10)
```

### 2. 특정 서비스의 로그

```javascript
db.application_logs.find({ 
  serviceName: "user-service",
  level: { $in: ["ERROR", "WARN"] }
})
.sort({ timestamp: -1 })
```

### 3. TraceId로 요청 전체 추적

```javascript
db.application_logs.find({ 
  traceId: "a1b2c3d4" 
})
.sort({ timestamp: 1 })
```

### 4. 특정 사용자 관련 로그

```javascript
db.application_logs.find({ 
  "metadata.userId": 12345 
})
.sort({ timestamp: -1 })
```

### 5. 느린 API 조회 (1초 이상)

```javascript
db.application_logs.find({ 
  duration: { $gt: 1000 } 
})
.sort({ duration: -1 })
```

### 6. 시간 범위 조회

```javascript
db.application_logs.find({
  timestamp: {
    $gte: ISODate("2025-01-15T00:00:00Z"),
    $lt: ISODate("2025-01-16T00:00:00Z")
  }
})
.sort({ timestamp: -1 })
```

### 7. 집계(Aggregation) - 서비스별 에러 카운트

```javascript
db.application_logs.aggregate([
  {
    $match: { 
      level: "ERROR",
      timestamp: { $gte: ISODate("2025-01-15T00:00:00Z") }
    }
  },
  {
    $group: {
      _id: "$serviceName",
      count: { $sum: 1 }
    }
  },
  {
    $sort: { count: -1 }
  }
])
```

### 8. 평균 API 응답 시간

```javascript
db.application_logs.aggregate([
  {
    $match: { 
      duration: { $exists: true },
      method: "GET"
    }
  },
  {
    $group: {
      _id: "$path",
      avgDuration: { $avg: "$duration" },
      count: { $sum: 1 }
    }
  },
  {
    $sort: { avgDuration: -1 }
  }
])
```

---

## 인덱스 생성

성능 최적화를 위해 MongoDB Compass 또는 Shell에서 인덱스를 생성하세요:

```javascript
// 1. 타임스탬프 인덱스 (최신순 조회용)
db.application_logs.createIndex({ timestamp: -1 })

// 2. 서비스명 + 타임스탬프 복합 인덱스
db.application_logs.createIndex({ 
  serviceName: 1, 
  timestamp: -1 
})

// 3. TraceId 인덱스 (요청 추적용)
db.application_logs.createIndex({ traceId: 1 })

// 4. 로그 레벨 + 타임스탬프 인덱스 (에러 조회용)
db.application_logs.createIndex({ 
  level: 1, 
  timestamp: -1 
})

// 5. TTL 인덱스 (30일 후 자동 삭제)
db.application_logs.createIndex(
  { timestamp: 1 },
  { expireAfterSeconds: 2592000 }  // 30일 = 30 * 24 * 60 * 60
)

// 6. Duration 인덱스 (느린 API 조회용)
db.application_logs.createIndex({ duration: -1 })

// 7. 메타데이터 인덱스 (사용자 ID 등)
db.application_logs.createIndex({ "metadata.userId": 1 })
```

---

## 트러블슈팅

### 1. 로그가 MongoDB에 저장되지 않음

#### 체크리스트:
- [ ] `logging.mongodb.enabled=true` 설정 확인
- [ ] MongoDB 연결 정보 확인 (`spring.data.mongodb.uri`)
- [ ] MongoDB 서비스 실행 확인: `docker ps | grep mongo`
- [ ] 애플리케이션 로그에서 에러 확인

#### 디버깅:

```bash
# MongoDB 연결 테스트
docker exec -it stockSimulator-mongo mongosh \
  -u stocksim -p stocksim123 --authenticationDatabase admin

# 데이터베이스 확인
use stocksimulator
db.application_logs.countDocuments()
```

### 2. MongoTemplate 주입 실패

**에러**: `MongoDbAppender not configured with MongoTemplate`

**해결**:
```kotlin
// LoggingConfig가 자동으로 MongoTemplate을 주입합니다
// @ConditionalOnProperty 조건 확인:
logging:
  mongodb:
    enabled: true  # 반드시 설정
```

### 3. 성능 이슈

#### 증상: 로깅으로 인한 API 응답 지연

**해결**:
```xml
<!-- logback-spring.xml에서 큐 크기 조정 -->
<appender name="ASYNC_MONGODB" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>1024</queueSize>         <!-- 기본 512 → 1024 -->
    <discardingThreshold>200</discardingThreshold> <!-- 부하 시 DEBUG 로그 버림 -->
    <appender-ref ref="MONGODB"/>
</appender>
```

### 4. MongoDB 디스크 공간 부족

**해결**: TTL 인덱스로 오래된 로그 자동 삭제

```javascript
// 7일만 보관 (개발 환경)
db.application_logs.createIndex(
  { timestamp: 1 },
  { expireAfterSeconds: 604800 }  // 7일
)

// 기존 데이터 수동 삭제
db.application_logs.deleteMany({
  timestamp: { $lt: ISODate("2025-01-01T00:00:00Z") }
})
```

### 5. 특정 서비스만 로그가 안됨

#### 체크리스트:
- [ ] 해당 서비스의 `logback-spring.xml`에 ASYNC_MONGODB appender 추가 확인
- [ ] `application.yml`에 MongoDB URI 설정 확인
- [ ] common 모듈 의존성 확인

```kotlin
// build.gradle.kts
dependencies {
    implementation(project(":backend:common"))
}
```

---

## 빌드 및 배포

### 1. Common 모듈 빌드

```bash
./gradlew :backend:common:build
```

### 2. 전체 재빌드

```bash
./gradlew clean build -x test
```

### 3. Docker Compose 재시작

```bash
docker-compose --profile all up -d --build
```

### 4. 로그 확인

```bash
# 서비스 로그
docker logs stockSimulator-user-service 2>&1 | grep "MongoDbAppender"

# MongoDB 데이터 확인
docker exec -it stockSimulator-mongo mongosh \
  -u stocksim -p stocksim123 --authenticationDatabase admin \
  --eval "use stocksimulator; db.application_logs.find().limit(5).pretty()"
```

---

## 참고 자료

- [Logback 공식 문서](http://logback.qos.ch/manual/index.html)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [MongoDB Aggregation](https://www.mongodb.com/docs/manual/aggregation/)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)

---

## 라이선스

이 로깅 시스템은 Stock-Simulator 프로젝트의 일부입니다.
