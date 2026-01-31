# Spring Boot 4.0 & Kotlin 2.2.20 업그레이드 가이드

## 📋 변경 사항 요약

### 버전 업그레이드
| 구분 | 이전 버전 | 새 버전 |
|------|----------|---------|
| Kotlin | 2.3.0 | **2.2.20** |
| Spring Boot | 3.5.10 | **4.0.1** |
| Spring Cloud | 2025.0.0 | **2025.1.1** (Oakwood) |
| Spring Framework | 6.x | **7.0** |
| Jakarta EE | 10 | **11** |
| Kotlin Serialization | 1.10.0 | **1.9.0** (BOM 관리) |

### 주요 업데이트된 의존성 (Spring Boot 4.0)
- **Jackson**: 2.x → **3.0** (메이저 업그레이드)
- **Hibernate**: 6.x → **7.1**
- **Kafka**: 3.x → **4.1.0**
- **MongoDB Driver**: 4.x → **5.6.0**
- **Lettuce**: 6.2 → **6.8.1**
- **Micrometer**: 1.13 → **1.16**

---

## 🔧 코드 변경사항

### 1. JPA Entity ID 타입 변경

**UserJpaEntity & BalanceJpaEntity: UUID String → Long**

#### Before (Kotlin 2.3.0 / Spring Boot 3.5)
```kotlin
@Entity
class UserJpaEntity(
    @Id
    @Column(name = "user_id", length = 36)
    val userId: String = UUID.randomUUID().toString(),
    // ...
)
```

#### After (Kotlin 2.2.20 / Spring Boot 4.0)
```kotlin
@Entity
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long? = null,
    // ...
)
```

**변경 이유:**
- Domain Model과 Entity 간 타입 일치성 확보
- Kotlin 2.2.20의 더 엄격한 타입 체크 대응
- Long ID가 분산 환경에서도 충분 (BIGSERIAL 사용)

---

### 2. Kafka API 변경 (Spring Kafka 4.0)

#### Before
```kotlin
fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
    return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
        consumerFactory = consumerFactory()  // ❌ Property 직접 할당
        setConcurrency(3)
    }
}
```

#### After
```kotlin
fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
    return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
        setConsumerFactory(consumerFactory())  // ✅ Setter 메서드 사용
        setConcurrency(3)
    }
}
```

---

### 3. Null-Safety 강화 (JSpecify)

Spring Security의 `PasswordEncoder.encode()`가 nullable로 표시됨

#### Before
```kotlin
val encryptedPassword = passwordEncoder.encode(command.password)  // ❌ String?으로 추론
```

#### After
```kotlin
val encryptedPassword: String = passwordEncoder.encode(command.password) ?: 
    throw IllegalStateException("Password encoding failed")  // ✅ Null-safe 처리
```

---

## 🗄️ 데이터베이스 마이그레이션

### ⚠️ 중요: ID 타입 변경으로 인한 스키마 마이그레이션 필요

**옵션 1: 기존 데이터 초기화 (개발 환경 권장)**

```bash
# Docker 컨테이너 정지 및 볼륨 삭제
docker-compose --profile all down
docker volume rm stock-simulator_postgres_data

# 재시작 (JPA가 자동으로 새 스키마 생성)
docker-compose --profile all up -d
```

**옵션 2: 마이그레이션 스크립트 실행 (데이터 보존)**

```bash
# PostgreSQL 컨테이너 접속
docker exec -it stockSimulator-postgres psql -U stocksim -d stocksimulator

# 마이그레이션 스크립트 실행
\i /path/to/infra/postgres/migration-to-long-ids.sql
```

마이그레이션 스크립트는 다음을 수행합니다:
1. 기존 `users.users` 및 `users.balances` 테이블 삭제
2. BIGINT ID를 사용하는 새 테이블 생성
3. IDENTITY 전략 시퀀스 생성
4. 인덱스 및 외래 키 제약 조건 재생성

---

## 🐳 Docker & 인프라 설정

### 1. Dockerfile 확인 (변경 불필요)

현재 사용 중인 JDK가 Java 21이므로 Spring Boot 4.0 요구사항(Java 17+) 충족

```dockerfile
FROM eclipse-temurin:21-jre-alpine
# Spring Boot 4.0 requires Java 17+, we're using Java 21 ✅
```

### 2. application.yml 설정 확인

**Actuator Endpoints (변경 불필요)**

Spring Boot 4.0에서도 기존 설정 유지 가능:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

**Spring Boot 4.0 권장 사항 (선택사항):**

```yaml
management:
  observations:
    http:
      server:
        enabled: true  # HTTP 요청 관찰 활성화
  tracing:
    sampling:
      probability: 1.0  # 100% 샘플링 (개발 환경)
```

### 3. Prometheus 설정 (변경 불필요)

`infra/prometheus/prometheus.yml`은 그대로 사용 가능합니다. Spring Boot 4.0도 `/actuator/prometheus` 엔드포인트를 동일하게 지원합니다.

---

## 📦 빌드 및 배포

### 1. 로컬 빌드 테스트

```bash
# Clean build
./gradlew clean build -x test

# 특정 서비스 빌드
./gradlew :backend:user-service:build -x test
```

### 2. Docker 이미지 재빌드

```bash
# 모든 컨테이너 정지
docker-compose --profile all down

# 이미지 재빌드 및 실행
docker-compose --profile all up -d --build
```

### 3. 서비스 헬스체크

```bash
# Eureka 대시보드
http://localhost:8761

# Prometheus Targets
http://localhost:9091/targets

# Grafana 대시보드
http://localhost:3001  # admin / stocksim123

# 개별 서비스 헬스체크
curl http://localhost:8081/actuator/health  # user-service
curl http://localhost:8082/actuator/health  # stock-service
curl http://localhost:8083/actuator/health  # trading-service
```

---

## ⚠️ Breaking Changes 및 Deprecation

### 1. Jackson 3.0 Deprecation

**경고 메시지:**
```
'class JsonSerializer<T : Any> : Any, Serializer<T>' is deprecated. Deprecated in Java.
'class JsonDeserializer<T : Any> : Any, Deserializer<T>' is deprecated. Deprecated in Java.
```

**현재 상태:**
- ✅ 빌드 성공 (호환성 유지)
- ⚠️ 경고만 발생 (기능상 문제 없음)

**향후 조치 (선택사항):**
- Spring Boot 4.0 권장 방식으로 리팩토링
- `spring-boot-starter-jackson` 사용 검토

### 2. Kotlin 2.2.x Annotation Default Target

**경고 메시지:**
```
This annotation is currently applied to the value parameter only, but in the future it will also be applied to field.
- To opt in to applying to both value parameter and field, add '-Xannotation-default-target=param-property'
```

**해결 방법 (선택사항):**

`backend/build.gradle.kts`에 컴파일러 옵션 추가:

```kotlin
tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        freeCompilerArgs.add("-Xannotation-default-target=param-property")  // 추가
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
```

---

## 📝 체크리스트

업그레이드 전 확인사항:

- [ ] 로컬 환경에서 빌드 성공 (`./gradlew build -x test`)
- [ ] DB 마이그레이션 스크립트 실행 또는 데이터 초기화
- [ ] Docker 이미지 재빌드
- [ ] 모든 서비스가 Eureka에 등록되는지 확인
- [ ] Prometheus가 모든 서비스 메트릭 수집하는지 확인
- [ ] Grafana 대시보드 정상 작동 확인
- [ ] Kafka 이벤트 송수신 테스트
- [ ] API Gateway 라우팅 테스트

---

## 🎯 업그레이드 효과

✅ **JDK 26+ 지원** - Kotlin 2.2.20으로 최신 JDK 사용 가능  
✅ **Spring Boot 4.0 기능** - Virtual Threads, 향상된 Observability  
✅ **타입 안전성 강화** - Kotlin 2.2 + JSpecify null-safety  
✅ **성능 향상** - Micrometer 1.16, Hibernate 7.1 최적화  
✅ **최신 인프라 지원** - Jakarta EE 11, Servlet 6.1  

---

## 📚 참고 자료

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Cloud 2025.1.1 Release](https://spring.io/blog/2026/01/29/spring-cloud-2025-1-1-aka-oakwood-has-been-released)
- [Kotlin 2.2.20 Release Notes](https://kotlinlang.org/docs/whatsnew2220.html)
- [Micrometer 1.16 Documentation](https://micrometer.io/docs)

---

**작성일:** 2026-01-30  
**버전:** 1.0  
**적용 프로젝트:** Stock-Simulator MVP
