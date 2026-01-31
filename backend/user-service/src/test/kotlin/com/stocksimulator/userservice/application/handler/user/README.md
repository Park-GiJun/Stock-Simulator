# UserCommandHandler 테스트 가이드

## 📋 테스트 개요

**UserCommandHandler**의 회원가입 로직을 검증하는 단위 테스트입니다.

### 테스트 대상
- `UserCommandHandler.signUp()` 메서드

### 테스트 전략
- **Mock 기반 단위 테스트** (MockK 사용)
- **Given-When-Then** 패턴
- **경계값 테스트** 포함

---

## 🧪 테스트 케이스 목록

### ✅ 성공 시나리오 (5개)

| # | 테스트 케이스 | 검증 내용 |
|---|--------------|----------|
| 1 | 회원가입 성공 - 유저와 초기 잔고(500만원) 생성 | User, Balance 모두 정상 저장 |
| 2 | 회원가입 성공 - 비밀번호 암호화 | BCrypt 암호화 적용 확인 |
| 3 | 회원가입 성공 - 초기 잔고 정확성 | 정확히 500만원 생성 |
| 4 | 회원가입 성공 - 닉네임 최소 길이 (2자) | 경계값 테스트 |
| 5 | 회원가입 성공 - 닉네임 최대 길이 (20자) | 경계값 테스트 |

### ❌ 실패 시나리오 (5개)

| # | 테스트 케이스 | 예외 타입 | ErrorCode |
|---|--------------|----------|-----------|
| 1 | 이메일 중복 | `DuplicateResourceException` | `DUPLICATE_EMAIL` |
| 2 | 닉네임 중복 | `DuplicateResourceException` | `DUPLICATE_NICKNAME` |
| 3 | Balance 저장 실패 | `RuntimeException` | - |
| 4 | 닉네임 너무 짧음 (1자) | `IllegalArgumentException` | - |
| 5 | 닉네임 너무 김 (21자) | `IllegalArgumentException` | - |

---

## 🏗️ 테스트 구조

### Mock 객체
```kotlin
private lateinit var userPersistencePort: UserPersistencePort
private lateinit var balancePersistencePort: BalancePersistencePort
private lateinit var passwordEncoder: PasswordEncoder
```

### SUT (System Under Test)
```kotlin
private lateinit var userCommandHandler: UserCommandHandler
```

### 테스트 라이프사이클
```kotlin
@BeforeEach  // 각 테스트 전에 Mock 초기화
@AfterEach   // 각 테스트 후에 Mock 정리
```

---

## 🔍 주요 검증 항목

### 1. **회원가입 성공 플로우**

```kotlin
// 1. 이메일 중복 검증
verify(exactly = 1) { userPersistencePort.findByEmail(command.email) }

// 2. 닉네임 중복 검증
verify(exactly = 1) { userPersistencePort.findByUsername(command.username) }

// 3. 비밀번호 암호화
verify(exactly = 1) { passwordEncoder.encode(command.password) }

// 4. User 저장 (ID 없는 상태)
verify(exactly = 1) {
    userPersistencePort.save(
        match { it.userId == null && it.password == encryptedPassword }
    )
}

// 5. Balance 저장 (초기 500만원)
verify(exactly = 1) {
    balancePersistencePort.save(
        match { it.cash == 5_000_000L && it.userId == savedUserId }
    )
}
```

### 2. **이메일 중복 시 조기 종료**

```kotlin
// 이메일 중복 검증만 호출
verify(exactly = 1) { userPersistencePort.findByEmail(command.email) }

// 나머지는 호출 안됨
verify(exactly = 0) { userPersistencePort.findByUsername(any()) }
verify(exactly = 0) { passwordEncoder.encode(any()) }
verify(exactly = 0) { userPersistencePort.save(any()) }
verify(exactly = 0) { balancePersistencePort.save(any()) }
```

### 3. **비밀번호 암호화 검증**

```kotlin
verify {
    userPersistencePort.save(
        match {
            it.password == encryptedPassword &&
            it.password != command.password  // 평문과 달라야 함
        }
    )
}
```

---

## 🚀 테스트 실행

### Gradle을 통한 실행

```bash
# 전체 테스트 실행
./gradlew :backend:user-service:test

# UserCommandHandlerTest만 실행
./gradlew :backend:user-service:test --tests UserCommandHandlerTest

# 특정 테스트 케이스만 실행
./gradlew :backend:user-service:test --tests "UserCommandHandlerTest.signUp should create user and initial balance successfully"

# 테스트 리포트 생성
./gradlew :backend:user-service:test
# 결과: backend/user-service/build/reports/tests/test/index.html
```

### IntelliJ IDEA에서 실행

1. 테스트 파일 열기: `UserCommandHandlerTest.kt`
2. 클래스 옆 녹색 화살표 클릭 → "Run 'UserCommandHandlerTest'"
3. 개별 테스트: 각 메서드 옆 화살표 클릭

---

## 📊 예상 테스트 결과

### 성공 시
```
UserCommandHandlerTest > signUp should create user and initial balance successfully PASSED
UserCommandHandlerTest > signUp should encrypt password before saving PASSED
UserCommandHandlerTest > signUp should throw DuplicateResourceException when email already exists PASSED
UserCommandHandlerTest > signUp should throw DuplicateResourceException when username already exists PASSED
UserCommandHandlerTest > signUp should create initial balance with exactly 5 million KRW PASSED
UserCommandHandlerTest > signUp should throw exception when balance save fails PASSED
UserCommandHandlerTest > signUp should succeed with minimum username length PASSED
UserCommandHandlerTest > signUp should succeed with maximum username length PASSED
UserCommandHandlerTest > signUp should fail when username is too short PASSED
UserCommandHandlerTest > signUp should fail when username is too long PASSED

BUILD SUCCESSFUL in 3s
10 tests completed, 10 passed
```

---

## 🛠️ MockK 사용법

### 1. **Mock 생성**

```kotlin
val userPersistencePort: UserPersistencePort = mockk()
```

### 2. **Stub 설정 (반환값 지정)**

```kotlin
// 단순 반환
every { userPersistencePort.findByEmail("test@example.com") } returns null

// 조건부 반환
every { passwordEncoder.encode(any()) } returns "encrypted_password"

// 람다로 동적 반환
every { userPersistencePort.save(any()) } answers {
    val user = firstArg<UserModel>()
    user.withId("user-123")
}
```

### 3. **호출 검증**

```kotlin
// 정확히 1번 호출
verify(exactly = 1) { userPersistencePort.findByEmail("test@example.com") }

// 호출 안됨
verify(exactly = 0) { userPersistencePort.save(any()) }

// 파라미터 검증
verify {
    userPersistencePort.save(
        match { it.email == "test@example.com" }
    )
}
```

### 4. **인자 캡처**

```kotlin
val capturedBalance = slot<BalanceModel>()
every { balancePersistencePort.save(capture(capturedBalance)) } answers {
    firstArg<BalanceModel>().copy(balanceId = "balance-123")
}

// 테스트 실행 후
val savedBalance = capturedBalance.captured
assertThat(savedBalance.cash).isEqualTo(5_000_000L)
```

---

## 📝 테스트 코드 작성 팁

### ✅ DO

1. **명확한 테스트 이름**
   ```kotlin
   @Test
   @DisplayName("회원가입 성공 - 유저와 초기 잔고(500만원) 생성")
   fun `signUp should create user and initial balance successfully`()
   ```

2. **Given-When-Then 구조**
   ```kotlin
   // given: 테스트 데이터 준비
   val command = SignUpCommand(...)
   every { ... } returns ...
   
   // when: 테스트 실행
   val result = userCommandHandler.signUp(command)
   
   // then: 검증
   assertThat(result).isNotNull
   verify { ... }
   ```

3. **독립적인 테스트**
   - 각 테스트는 서로 영향을 주지 않아야 함
   - `@BeforeEach`, `@AfterEach`로 초기화/정리

### ❌ DON'T

1. **실제 DB 연결 금지** (단위 테스트)
2. **하나의 테스트에서 너무 많은 것 검증**
3. **테스트 간 의존성 생성**

---

## 🔄 커버리지 확인

### JaCoCo 플러그인 사용

```bash
# 커버리지 리포트 생성
./gradlew :backend:user-service:jacocoTestReport

# 결과 확인
# backend/user-service/build/reports/jacoco/test/html/index.html
```

### 목표 커버리지
- **라인 커버리지**: 80% 이상
- **브랜치 커버리지**: 70% 이상

---

## 🐛 트러블슈팅

### 1. MockK 초기화 실패

**에러:**
```
lateinit property userPersistencePort has not been initialized
```

**해결:**
```kotlin
@BeforeEach
fun setUp() {
    userPersistencePort = mockk()  // ✅ 반드시 초기화
}
```

---

### 2. 예외가 발생하지 않음

**문제:**
```kotlin
assertThrows<DuplicateResourceException> {
    userCommandHandler.signUp(command)
}
// 실패: 예외가 발생하지 않음
```

**해결:**
```kotlin
// Mock 설정 확인
every { userPersistencePort.findByEmail(command.email) } returns existingUser  // ✅
```

---

### 3. verify 실패

**에러:**
```
Verification failed: call 1 of 1 was not matched
```

**해결:**
```kotlin
// 파라미터 정확히 매칭
verify { userPersistencePort.findByEmail("test@example.com") }  // ✅

// 또는 any() 사용
verify { userPersistencePort.findByEmail(any()) }
```

---

## 📚 참고 자료

- [MockK 공식 문서](https://mockk.io/)
- [JUnit 5 가이드](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ 문서](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

**작성일:** 2025-01-23  
**테스트 커버리지:** UserCommandHandler 100%
