# User Service - Redis Session 기반 로그인 구현 완료 ✅

## 📋 구현 내용

### 1. Spring Session Data Redis 의존성 추가 ✅
**파일**: `build.gradle.kts`

```kotlin
// Spring Session (Redis - WebFlux)
implementation("org.springframework.session:spring-session-data-redis")
implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
```

- WebFlux 환경에서 Reactive Redis 사용
- Session 데이터를 Redis에 저장

---

### 2. Redis Session 설정 ✅
**파일**: 
- `application.yml` - Session 타임아웃, Redis 네임스페이스 설정
- `adapter/in/web/config/RedisSessionConfig.kt` - WebFlux Session 설정

#### application.yml
```yaml
spring:
  session:
    store-type: redis
    timeout: 30m
    redis:
      namespace: stock-simulator:session
      flush-mode: on_save
```

#### RedisSessionConfig.kt
```kotlin
@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 1800) // 30분
class RedisSessionConfig {
    @Bean
    fun webSessionIdResolver(): WebSessionIdResolver {
        val resolver = CookieWebSessionIdResolver()
        resolver.setCookieName("SESSION")
        resolver.setCookieMaxAge(Duration.ofMinutes(30))
        return resolver
    }
}
```

**주요 설정**:
- Session 만료 시간: **30분**
- Cookie 이름: `SESSION`
- Redis 네임스페이스: `stock-simulator:session`
- HttpOnly, SameSite 적용

---

### 3. Application Layer 구현 ✅

#### DTO (Command/Result)
- **LoginCommand**: 로그인 요청 데이터 (email, password)
- **LoginResult**: 로그인 결과 (userId, email, username, role)
- **UserResult**: 사용자 정보 (userId, email, username, role)

#### UseCase Interface
- **LoginUseCase**: 로그인 처리
- **GetCurrentUserUseCase**: 현재 사용자 정보 조회

#### Handler Implementation
**파일**: `application/handler/user/UserQueryHandler.kt`

```kotlin
@Service
class UserQueryHandler(
    private val userPersistencePort: UserPersistencePort,
    private val passwordEncoder: PasswordEncoder
) : LoginUseCase, GetCurrentUserUseCase {
    
    override fun login(command: LoginCommand): LoginResult {
        // 1. 이메일로 사용자 조회
        // 2. 비밀번호 검증 (BCrypt)
        // 3. LoginResult 반환
    }
    
    override fun getCurrentUser(userId: Long): UserResult {
        // 사용자 정보 조회
    }
}
```

**검증 로직**:
- 이메일 존재 여부 확인
- BCrypt 비밀번호 매칭
- 실패 시 동일한 에러 메시지 ("이메일 또는 비밀번호가 일치하지 않습니다") - 보안 강화

---

### 4. Web Layer DTO ✅
**파일**: 
- `adapter/in/web/dto/LoginRequest.kt`
- `adapter/in/web/dto/LoginResponse.kt`
- `adapter/in/web/dto/UserResponse.kt`

#### LoginRequest
```kotlin
data class LoginRequest(
    @Email @NotBlank
    val email: String,
    
    @NotBlank
    val password: String
)
```

#### LoginResponse
```kotlin
data class LoginResponse(
    val userId: Long,
    val email: String,
    val username: String,
    val role: String,
    val sessionId: String  // Redis Session ID
)
```

#### UserResponse
```kotlin
data class UserResponse(
    val userId: Long,
    val email: String,
    val username: String,
    val role: String
)
```

---

### 5. UserWebAdapter 확장 ✅

**새로운 엔드포인트**:

#### POST /api/v1/users/login
- 이메일/비밀번호 검증
- Session에 userId 저장
- Session ID 반환 (Cookie로도 전달)

#### POST /api/v1/users/logout
- Session 무효화 (`webSession.invalidate()`)
- Redis에서 세션 삭제

#### GET /api/v1/users/me
- Session에서 userId 추출
- 사용자 정보 조회 및 반환
- 인증 실패 시 401 Unauthorized

---

## 🏗️ 아키텍처 레이어

### Session 기반 인증 플로우

```
[로그인]
1. POST /api/v1/users/login
   ↓
2. LoginRequest validation
   ↓
3. UserQueryHandler.login()
   ├─ findByEmail()
   ├─ passwordEncoder.matches()
   └─ LoginResult 반환
   ↓
4. WebSession에 userId 저장
   ↓
5. Redis에 Session 자동 저장
   ↓
6. Set-Cookie: SESSION={sessionId}
   ↓
7. 클라이언트: Cookie 저장

[인증된 요청]
1. GET /api/v1/users/me (Cookie: SESSION=xxx)
   ↓
2. Spring Session Filter
   ├─ Redis에서 Session 조회
   ├─ userId 추출
   └─ WebSession 객체 생성
   ↓
3. UserWebAdapter.getCurrentUser()
   ├─ session.getAttribute("userId")
   └─ UseCase 호출
   ↓
4. UserResponse 반환

[로그아웃]
1. POST /api/v1/users/logout
   ↓
2. webSession.invalidate()
   ↓
3. Redis에서 Session 삭제
   ↓
4. Cookie 만료
```

---

## 📡 API 명세

### 1. POST /api/v1/users/login

#### Request
```json
{
  "email": "test@example.com",
  "password": "Test1234!"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "test@example.com",
    "username": "테스트유저",
    "role": "ROLE_USER",
    "sessionId": "abc123..."
  },
  "message": "로그인에 성공했습니다",
  "timestamp": 1704067200000
}
```

#### Response Headers
```
Set-Cookie: SESSION=abc123...; Path=/; HttpOnly; SameSite=Lax; Max-Age=1800
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "이메일 또는 비밀번호가 일치하지 않습니다",
  "errorCode": "U004",
  "timestamp": 1704067200000
}
```

---

### 2. GET /api/v1/users/me

#### Request Headers
```
Cookie: SESSION=abc123...
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "test@example.com",
    "username": "테스트유저",
    "role": "ROLE_USER"
  },
  "timestamp": 1704067200000
}
```

#### Error Response (401 Unauthorized)
```json
{
  "success": false,
  "message": "인증이 필요합니다",
  "errorCode": "A001",
  "timestamp": 1704067200000
}
```

---

### 3. POST /api/v1/users/logout

#### Request Headers
```
Cookie: SESSION=abc123...
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "로그아웃되었습니다",
  "timestamp": 1704067200000
}
```

---

## 💾 Redis 데이터 구조

### Session Key-Value
```
Key: stock-simulator:session:sessions:{sessionId}
Type: Hash

Value:
{
  "sessionAttr:userId": 1,
  "creationTime": 1704067200000,
  "lastAccessTime": 1704067200000,
  "maxInactiveInterval": 1800
}

TTL: 1800초 (30분)
```

### Redis 명령어로 확인
```bash
# Redis CLI 접속
docker exec -it stockSimulator-redis redis-cli -a stocksim123

# 모든 세션 조회
KEYS stock-simulator:session:*

# 특정 세션 조회
HGETALL stock-simulator:session:sessions:{sessionId}

# 세션 TTL 확인
TTL stock-simulator:session:sessions:{sessionId}

# 세션 강제 삭제
DEL stock-simulator:session:sessions:{sessionId}
```

---

## ✅ 검증 완료 사항

### 1. 필수 의존성 ✅
- [x] `spring-session-data-redis`
- [x] `spring-boot-starter-data-redis-reactive`
- [x] `spring-boot-starter-security` (BCryptPasswordEncoder)
- [x] `spring-boot-starter-validation`

### 2. 설정 파일 ✅
- [x] `application.yml` - Session, Redis 설정
- [x] `RedisSessionConfig` - WebFlux Session 설정
- [x] `SecurityConfig` - PasswordEncoder Bean

### 3. Application Layer ✅
- [x] `LoginUseCase`, `GetCurrentUserUseCase` 인터페이스
- [x] `UserQueryHandler` 구현
- [x] `LoginCommand`, `LoginResult`, `UserResult` DTO

### 4. Web Layer ✅
- [x] `LoginRequest`, `LoginResponse`, `UserResponse` DTO
- [x] `UserWebAdapter` - 로그인/로그아웃/현재사용자 엔드포인트

### 5. 보안 ✅
- [x] BCrypt 비밀번호 암호화
- [x] Session HttpOnly Cookie
- [x] Session 30분 자동 만료
- [x] 로그인 실패 시 동일한 에러 메시지 (보안)

---

## 🧪 테스트 방법

### 1. 로컬 환경 실행
```bash
# Redis 실행 확인
docker ps | grep redis

# user-service 실행
./gradlew :backend:user-service:bootRun

# 또는 Docker Compose
docker-compose --profile all up -d
```

### 2. HTTP 클라이언트 테스트
`test-requests.http` 파일 참조 (17가지 테스트 케이스 포함)

**주요 테스트 시나리오**:
1. 회원가입
2. 로그인 (Session Cookie 자동 저장)
3. 현재 사용자 조회 (Cookie 자동 전송)
4. 로그아웃 (Session 무효화)
5. 로그아웃 후 조회 실패 (401)
6. 비밀번호 오류
7. 존재하지 않는 이메일
8. Validation 오류
9. 동시 로그인 (여러 세션)

### 3. cURL 테스트
```bash
# 로그인
curl -c cookies.txt -X POST http://localhost:8081/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234!"}'

# 현재 사용자 조회 (Cookie 사용)
curl -b cookies.txt http://localhost:8081/api/v1/users/me

# 로그아웃
curl -b cookies.txt -X POST http://localhost:8081/api/v1/users/logout
```

### 4. Swagger UI
- Local: http://localhost:8081/swagger-ui.html
- Gateway: http://localhost:9832/user-service/swagger-ui.html

⚠️ **주의**: Swagger UI는 Cookie를 자동으로 저장하지 않으므로 cURL이나 IntelliJ HTTP Client 사용 권장

---

## 🔒 보안 고려사항

### 1. Session Hijacking 방지
- ✅ HttpOnly Cookie 사용 (JavaScript 접근 불가)
- ✅ SameSite=Lax (CSRF 기본 방어)
- ⚠️ HTTPS 사용 권장 (프로덕션 환경)

### 2. Session Fixation 방지
- ✅ 로그인 시 새로운 Session ID 생성
- ✅ Spring Security 기본 제공

### 3. Brute Force 방지
- ⚠️ 로그인 실패 횟수 제한 미구현 (향후 추가)
- ⚠️ Redis 기반 Rate Limiting 추가 권장

### 4. 비밀번호 보안
- ✅ BCrypt 암호화 (강도 조절 가능)
- ✅ 로그인 실패 시 동일한 에러 메시지

---

## 🚀 빌드 확인

```bash
✅ BUILD SUCCESSFUL in 7s
8 actionable tasks: 5 executed, 3 up-to-date
```

모든 컴파일 오류 없음, Redis Session 기반 로그인 완성! 🎉

---

## 🎯 다음 단계 (TODO)

### 1. Session 기반 인증 필터 추가 (선택)
- [ ] `SessionAuthenticationWebFilter` 구현
- [ ] SecurityContext에 인증 정보 설정
- [ ] `@AuthenticationPrincipal` 지원

### 2. 사용자 정보 수정/탈퇴
- [ ] PUT `/api/v1/users/me` - 닉네임 변경
- [ ] DELETE `/api/v1/users/me` - 회원 탈퇴

### 3. 잔고 조회
- [ ] GET `/api/v1/users/me/balance` - 내 잔고 조회

### 4. Rate Limiting (로그인 실패 제한)
- [ ] Redisson 기반 Rate Limiter 구현
- [ ] 5회 실패 시 5분간 차단

### 5. Session 모니터링
- [ ] Grafana 대시보드에 Session 수 추가
- [ ] Redis Session 메트릭 수집

### 6. MSA 환경 Session 공유
- [ ] API Gateway에서 Session 검증
- [ ] 다른 서비스에서도 동일한 Session 사용

---

## 📚 생성/수정된 파일

### 수정된 파일 (2개)
1. ✅ `build.gradle.kts` - Spring Session 의존성 추가
2. ✅ `application.yml` - Redis Session 설정

### 생성된 파일 (12개)

#### Config (1개)
3. ✅ `adapter/in/web/config/RedisSessionConfig.kt`

#### Application Layer (6개)
4. ✅ `application/port/in/user/LoginUseCase.kt`
5. ✅ `application/port/in/user/GetCurrentUserUseCase.kt`
6. ✅ `application/dto/command/user/LoginCommand.kt`
7. ✅ `application/dto/result/user/LoginResult.kt`
8. ✅ `application/dto/result/user/UserResult.kt`
9. ✅ `application/handler/user/UserQueryHandler.kt`

#### Web Layer (4개)
10. ✅ `adapter/in/web/dto/LoginRequest.kt`
11. ✅ `adapter/in/web/dto/LoginResponse.kt`
12. ✅ `adapter/in/web/dto/UserResponse.kt`
13. ✅ `adapter/in/web/UserWebAdapter.kt` (확장)

#### 문서/테스트 (2개)
14. ✅ `test-requests.http` (업데이트)
15. ✅ `REDIS_SESSION_LOGIN_GUIDE.md` (이 문서)

---

## 📖 참고 문서

### Spring Session 공식 문서
- [Spring Session - Redis](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)
- [Spring Session - WebFlux](https://docs.spring.io/spring-session/reference/web-flux.html)

### Redis Session 구조
- [Spring Session Data Redis Internals](https://docs.spring.io/spring-session/reference/api/redis.html)

### Cookie 보안
- [OWASP - Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)

---

## 💡 팁

### Session 디버깅
```kotlin
// WebAdapter에서 Session 정보 확인
fun debug(webSession: WebSession) {
    logger.debug("Session ID: ${webSession.id}")
    logger.debug("Creation Time: ${webSession.creationTime}")
    logger.debug("Last Access Time: ${webSession.lastAccessTime}")
    logger.debug("Max Inactive Interval: ${webSession.maxIdleTime}")
    logger.debug("Attributes: ${webSession.attributes}")
}
```

### Redis 모니터링
```bash
# Redis 실시간 모니터링
docker exec -it stockSimulator-redis redis-cli -a stocksim123 MONITOR

# Session 통계
docker exec -it stockSimulator-redis redis-cli -a stocksim123 INFO keyspace
```

### Session 만료 시간 동적 변경
```kotlin
// WebAdapter에서
webSession.maxIdleTime = Duration.ofHours(1)  // 1시간으로 연장
```

---

Redis Session 기반 인증 구현 완료! 🎊
