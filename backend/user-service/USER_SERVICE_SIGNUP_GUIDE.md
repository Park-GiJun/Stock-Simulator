# User Service - 회원가입 구현 완료 ✅

## 📋 구현 내용

### 1. PasswordEncoder Bean 추가 ✅
**파일**: `adapter/in/web/config/SecurityConfig.kt`

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
}
```

- BCrypt 알고리즘으로 비밀번호 암호화
- `UserCommandHandler`에서 주입받아 사용

### 2. Web Layer DTO 생성 ✅
**파일**: 
- `adapter/in/web/dto/SignUpRequest.kt`
- `adapter/in/web/dto/SignUpResponse.kt`

#### SignUpRequest (요청 DTO)
```kotlin
data class SignUpRequest(
    @Email @NotBlank @Size(max=100)
    val email: String,
    
    @NotBlank @Size(min=2, max=20)
    @Pattern(regexp="^[a-zA-Z0-9가-힣]+$")
    val username: String,
    
    @NotBlank @Size(min=8, max=20)
    @Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$")
    val password: String
)
```

**검증 규칙**:
- **이메일**: 이메일 형식, 최대 100자
- **닉네임**: 2~20자, 영문/한글/숫자만 허용
- **비밀번호**: 8~20자, 영문+숫자+특수문자 조합

#### SignUpResponse (응답 DTO)
```kotlin
data class SignUpResponse(
    val userId: Long,
    val email: String,
    val username: String
)
```

### 3. UserWebAdapter 완성 ✅
**파일**: `adapter/in/web/UserWebAdapter.kt`

```kotlin
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 API")
class UserWebAdapter(
    private val signUpUseCase: SignUpUseCase
) {
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "...")
    fun signUp(@Valid @RequestBody request: SignUpRequest): 
        Mono<ResponseEntity<ApiResponse<SignUpResponse>>>
}
```

**특징**:
- Spring WebFlux 기반 (Reactive)
- `@Valid` 어노테이션으로 자동 검증
- `ApiResponse<T>` 래핑으로 일관된 응답 형식
- HTTP 201 Created 응답
- Swagger 문서화 완료

---

## 🏗️ 아키텍처 레이어

### 1. Web Layer (Adapter In)
```
UserWebAdapter
  ↓ (SignUpRequest)
  ↓ → SignUpCommand 변환
```

### 2. Application Layer
```
SignUpUseCase (Interface)
  ↓
UserCommandHandler (Implementation)
  ├─ UserPersistencePort (User 저장)
  ├─ BalancePersistencePort (잔고 생성)
  └─ PasswordEncoder (비밀번호 암호화)
```

### 3. Domain Layer
```
UserModel
  ├─ create() - 신규 사용자 생성
  ├─ withId() - ID 할당
  └─ validation - 도메인 규칙 검증

BalanceModel
  └─ create() - 초기 잔고 5,000,000원
```

### 4. Persistence Layer (Adapter Out)
```
UserPersistenceAdapter
  ↓
UserJpaRepository
  ↓
PostgreSQL (users schema)
```

---

## 📡 API 명세

### POST /api/v1/users/signup

#### Request
```json
{
  "email": "user@example.com",
  "username": "테스트유저",
  "password": "Test1234!"
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "username": "테스트유저"
  },
  "message": "회원가입이 완료되었습니다",
  "timestamp": 1704067200000
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "email: 이메일 형식이 올바르지 않습니다",
  "errorCode": "C004",
  "timestamp": 1704067200000
}
```

#### Error Response (409 Conflict)
```json
{
  "success": false,
  "message": "이미 사용 중인 이메일입니다",
  "errorCode": "U002",
  "timestamp": 1704067200000
}
```

---

## ✅ 검증 완료 사항

### 1. 필수 의존성 ✅
- [x] `spring-boot-starter-webflux`
- [x] `spring-boot-starter-validation`
- [x] `spring-boot-starter-security`
- [x] `spring-boot-starter-data-jpa`
- [x] `springdoc-openapi-starter-webflux-ui`
- [x] `postgresql`

### 2. 설정 파일 ✅
- [x] `application.yml` - PostgreSQL, Redis, Kafka, Eureka 설정
- [x] `application-docker.yml` - Docker 환경 설정
- [x] Flyway 마이그레이션 (`V1__create_user_tables.sql`)

### 3. 보안 설정 ✅
- [x] PasswordEncoder Bean 등록
- [x] `/api/v1/users/signup` 엔드포인트 permitAll
- [x] CSRF 비활성화 (API 서버)

### 4. 예외 처리 ✅
- [x] `GlobalExceptionHandler` - 전역 예외 처리
- [x] `BusinessException` - 비즈니스 예외
- [x] `DuplicateResourceException` - 중복 리소스 (이메일/닉네임)
- [x] `WebExchangeBindException` - Validation 오류

### 5. 데이터베이스 ✅
- [x] Users 테이블 (user_id, email, username, password, role, timestamps)
- [x] Balances 테이블 (balance_id, user_id, cash=5,000,000, ...)
- [x] 인덱스 (idx_users_email, idx_balances_user_id)
- [x] Foreign Key (balances → users, ON DELETE CASCADE)

---

## 🧪 테스트 방법

### 1. 로컬 환경 테스트
```bash
# user-service 실행
./gradlew :backend:user-service:bootRun

# 또는 Docker Compose
docker-compose --profile all up -d
```

### 2. cURL 테스트
```bash
curl -X POST http://localhost:8081/api/v1/users/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "테스트유저",
    "password": "Test1234!"
  }'
```

### 3. IntelliJ HTTP Client
`test-requests.http` 파일 참조 (10가지 테스트 케이스 포함)

### 4. Swagger UI
- Local: http://localhost:8081/swagger-ui.html
- Gateway: http://localhost:9832/user-service/swagger-ui.html

---

## 🔄 회원가입 프로세스 플로우

```
1. 클라이언트 요청
   ↓
2. UserWebAdapter (Validation)
   ↓
3. SignUpCommand 생성
   ↓
4. UserCommandHandler.signUp()
   ├─ 4-1. 이메일 중복 체크
   ├─ 4-2. 닉네임 중복 체크
   ├─ 4-3. 비밀번호 BCrypt 암호화
   ├─ 4-4. UserModel 생성
   ├─ 4-5. User 저장 (PostgreSQL)
   ├─ 4-6. BalanceModel 생성 (5,000,000원)
   └─ 4-7. Balance 저장
   ↓
5. SignUpResult 반환
   ↓
6. SignUpResponse 변환
   ↓
7. ApiResponse 래핑 (HTTP 201)
   ↓
8. 클라이언트 응답
```

---

## 🎯 다음 단계 (TODO)

### 1. 로그인 기능 구현
- [ ] LoginRequest/LoginResponse DTO
- [ ] JWT 토큰 생성 (`JwtTokenProvider`)
- [ ] POST `/api/v1/users/login`
- [ ] Refresh Token 처리

### 2. 사용자 조회/수정/탈퇴
- [ ] GET `/api/v1/users/me` - 내 정보 조회
- [ ] PUT `/api/v1/users/me` - 내 정보 수정 (닉네임 변경)
- [ ] DELETE `/api/v1/users/me` - 회원 탈퇴

### 3. 잔고 조회
- [ ] GET `/api/v1/users/me/balance` - 내 잔고 조회

### 4. 통합 테스트
- [ ] `UserWebAdapterTest` (WebFlux Test)
- [ ] `UserCommandHandlerTest` (이미 존재)

### 5. Kafka 이벤트 발행
- [ ] 회원가입 시 `user.registered` 이벤트 발행
- [ ] Trading Service에서 NPC와 동일하게 처리

---

## 📚 관련 파일

### 생성된 파일
1. `adapter/in/web/dto/SignUpRequest.kt`
2. `adapter/in/web/dto/SignUpResponse.kt`
3. `adapter/in/web/UserWebAdapter.kt`
4. `test-requests.http`
5. `USER_SERVICE_SIGNUP_GUIDE.md` (이 파일)

### 수정된 파일
1. `adapter/in/web/config/SecurityConfig.kt`

### 기존 파일 (이미 완성)
1. `domain/UserModel.kt`
2. `domain/BalanceModel.kt`
3. `application/handler/user/UserCommandHandler.kt`
4. `application/port/in/user/SignUpUseCase.kt`
5. `application/dto/command/user/SignUpCommand.kt`
6. `application/dto/result/user/SignUpResult.kt`
7. `adapter/out/persistence/user/UserPersistenceAdapter.kt`
8. `adapter/out/persistence/balance/BalancePersistenceAdapter.kt`

---

## 🚀 빌드 확인

```bash
✅ BUILD SUCCESSFUL in 8s
8 actionable tasks: 4 executed, 4 up-to-date
```

모든 컴파일 오류 없음, 회원가입 기능 완성! 🎉
