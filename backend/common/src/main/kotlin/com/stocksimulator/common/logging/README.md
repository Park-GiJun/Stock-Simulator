# MongoDB 커스텀 로깅 시스템

## 🎯 빠른 시작

### 1. 로깅 사용하기

```kotlin
import com.stocksimulator.common.logging.CustomLogger

class YourController {
    private val log = CustomLogger(YourController::class.java)
    
    fun yourMethod() {
        log.info("Operation completed", mapOf(
            "userId" to 123,
            "action" to "register"
        ))
    }
}
```

### 2. MongoDB에서 로그 확인

```javascript
// MongoDB Compass 또는 Shell
db.application_logs.find().sort({ timestamp: -1 }).limit(10)
```

### 3. 특정 요청 추적

```javascript
// TraceId로 전체 흐름 추적
db.application_logs.find({ traceId: "abc123" }).sort({ timestamp: 1 })
```

---

## 📁 파일 구조

```
backend/common/src/main/kotlin/com/stocksimulator/common/logging/
├── LogDocument.kt           # MongoDB 로그 문서 모델
├── LogRepository.kt         # MongoDB Repository
├── MongoDbAppender.kt       # Logback Appender
├── CustomLogger.kt          # 래퍼 Logger (메인 사용)
├── LoggingConfig.kt         # Spring Configuration
└── example/
    └── CustomLoggerExamples.kt  # 사용 예시
```

---

## 🚀 주요 기능

| 기능 | 설명 |
|------|------|
| **구조화된 로깅** | 메타데이터(userId, orderId 등)를 JSON으로 저장 |
| **TraceId 추적** | 분산 시스템에서 요청 전체 추적 가능 |
| **API 자동 로깅** | API Gateway에서 모든 HTTP 요청/응답 자동 기록 |
| **Eureka 이벤트** | 서비스 등록/해제 자동 기록 |
| **비동기 처리** | 애플리케이션 성능에 영향 없음 |
| **유연한 쿼리** | MongoDB aggregation으로 강력한 분석 가능 |

---

## 📊 로그 구조

```javascript
{
  "_id": "...",
  "timestamp": ISODate("2025-01-15T10:30:45.123Z"),
  "serviceName": "user-service",
  "level": "INFO",
  "traceId": "a1b2c3d4",
  "threadName": "http-nio-8081-exec-1",
  "logger": "UserController",
  "message": "User registered",
  "metadata": {
    "userId": 12345,
    "email": "user@example.com"
  }
}
```

---

## 🔍 유용한 쿼리

### 최근 에러 로그
```javascript
db.application_logs.find({ level: "ERROR" })
  .sort({ timestamp: -1 })
  .limit(10)
```

### 특정 사용자 활동
```javascript
db.application_logs.find({ "metadata.userId": 12345 })
  .sort({ timestamp: -1 })
```

### 느린 API (1초 이상)
```javascript
db.application_logs.find({ duration: { $gt: 1000 } })
  .sort({ duration: -1 })
```

### 서비스별 에러 통계
```javascript
db.application_logs.aggregate([
  { $match: { level: "ERROR" } },
  { $group: { _id: "$serviceName", count: { $sum: 1 } } },
  { $sort: { count: -1 } }
])
```

---

## ⚙️ 설정

### application.yml
```yaml
logging:
  mongodb:
    enabled: true
```

### 환경 변수
```bash
LOGGING_MONGODB_ENABLED=true
```

---

## 📚 상세 문서

- [MongoDB 커스텀 로깅 가이드](../../doc/MongoDB_커스텀_로깅_가이드.md)
- [사용 예시](example/CustomLoggerExamples.kt)

---

## 🛠️ 트러블슈팅

### 로그가 저장되지 않음
```bash
# MongoDB 연결 확인
docker exec -it stockSimulator-mongo mongosh \
  -u stocksim -p stocksim123 --authenticationDatabase admin

# 데이터 확인
use stocksimulator
db.application_logs.countDocuments()
```

### 성능 이슈
```xml
<!-- logback-spring.xml에서 큐 크기 조정 -->
<appender name="ASYNC_MONGODB" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>1024</queueSize>
    <appender-ref ref="MONGODB"/>
</appender>
```

---

## 📝 라이선스

Stock-Simulator 프로젝트의 일부입니다.
