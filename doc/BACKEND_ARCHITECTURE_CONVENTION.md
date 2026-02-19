# Backend Architecture Convention

> Kotlin + Spring Boot 기반 MSA 프로젝트의 공통 아키텍처 컨벤션.
> 다른 프로젝트에서 그대로 재사용할 수 있도록 도메인 비종속적으로 작성됨.

---

## 1. 프로젝트 구조

### 1.1 전체 모듈 구조

```
project-root/
├── build.gradle.kts              # 루트 Gradle (플러그인 선언만)
├── settings.gradle.kts           # 모든 모듈 include
├── backend/
│   ├── build.gradle.kts          # 백엔드 공통 설정 (subprojects)
│   ├── common/                   # 공유 모듈 (DTO, Exception, Event, Config, Util)
│   ├── eureka-server/            # 서비스 디스커버리
│   ├── api-gateway/              # API 게이트웨이
│   ├── {domain}-service/         # 도메인 서비스 (N개)
│   └── ...
└── frontend/
```

### 1.2 서비스 내부 패키지 구조 (Hexagonal Architecture)

```
com.{company}.{service}/
├── domain/                           # 순수 도메인 (프레임워크 의존 없음)
│   ├── model/                        #   도메인 모델 (data class)
│   ├── enums/                        #   서비스 전용 Enum
│   ├── service/                      #   도메인 서비스 (복잡한 비즈니스 로직)
│   └── vo/                           #   Value Object
│
├── application/                      # 유스케이스 계층
│   ├── port/
│   │   ├── in/                       #   인바운드 포트 (UseCase 인터페이스)
│   │   │   └── {aggregate}/          #     집합체별 그룹핑
│   │   └── out/                      #   아웃바운드 포트 (외부 의존 인터페이스)
│   │       └── {aggregate}/          #     집합체별 그룹핑
│   ├── dto/
│   │   ├── command/                  #   쓰기 요청 DTO
│   │   │   └── {aggregate}/
│   │   ├── query/                    #   읽기 요청 DTO
│   │   │   └── {aggregate}/
│   │   └── result/                   #   응답 DTO
│   │       └── {aggregate}/
│   └── handler/                      #   UseCase 구현체 (Handler)
│       └── {aggregate}/
│
└── infrastructure/                   # 프레임워크 & 외부 의존
    ├── config/                       #   설정 클래스 (Security, Redis, Swagger 등)
    └── adapter/
        ├── in/                       #   인바운드 어댑터
        │   ├── web/                  #     REST Controller
        │   │   └── dto/              #       Request/Response DTO
        │   └── event/                #     Kafka Consumer
        └── out/                      #   아웃바운드 어댑터
            ├── persistence/          #     JPA 영속성
            │   └── {aggregate}/
            │       ├── adapter/      #       Port 구현체
            │       ├── entity/       #       JPA Entity
            │       └── repository/   #       JPA Repository
            ├── event/                #     Kafka Producer
            ├── cache/                #     Redis Cache
            └── client/               #     Feign Client (외부 서비스 호출)
```

---

## 2. 네이밍 컨벤션

### 2.1 클래스 네이밍

| 계층 | 접미사 | 예시 | 설명 |
|------|--------|------|------|
| **Domain Model** | `Model` | `UserModel`, `OrderModel` | 순수 도메인 객체 |
| **Domain Service** | `DomainService` | `OrderBookDomainService`, `PriceCalculatorDomainService` | 복잡한 도메인 로직 |
| **Value Object** | `Vo` | `MatchResultVo`, `OrderEntryVo` | 불변 값 객체 |
| **UseCase (In Port)** | `UseCase` | `PlaceOrderUseCase`, `GetStockListUseCase` | 인바운드 포트 인터페이스 |
| **Out Port** | `Port` | `OrderPersistencePort`, `TradingEventPort` | 아웃바운드 포트 인터페이스 |
| **Handler** | `CommandHandler` / `QueryHandler` | `OrderCommandHandler`, `StockQueryHandler` | UseCase 구현체 |
| **Web Adapter** | `WebAdapter` | `UserWebAdapter`, `StockWebAdapter` | REST Controller |
| **Persistence Adapter** | `PersistenceAdapter` | `UserPersistenceAdapter` | JPA Port 구현 |
| **Cache Adapter** | `CacheAdapter` | `RedisOrderBookCacheAdapter` | Redis Port 구현 |
| **Event Publisher** | `EventPublisher` | `KafkaTradingEventPublisher` | Kafka Producer |
| **Event Consumer** | `EventConsumer` | `StockEventConsumer` | Kafka Consumer |
| **Feign Client** | `FeignClient` | `StockServiceFeignClient` | 외부 서비스 호출 |
| **JPA Entity** | `JpaEntity` | `UserJpaEntity`, `OrderJpaEntity` | 영속성 엔티티 |
| **JPA Repository** | `JpaRepository` | `UserJpaRepository` | Spring Data 인터페이스 |
| **Config** | `Config` | `SecurityConfig`, `RedissonConfig` | 설정 클래스 |
| **Command DTO** | `Command` | `PlaceOrderCommand`, `SignUpCommand` | 쓰기 요청 |
| **Query DTO** | `Query` | `StockListQuery`, `OrderBookQuery` | 읽기 요청 |
| **Result DTO** | `Result` | `PlaceOrderResult`, `StockDetailResult` | 응답 (Application 계층) |
| **Request DTO** | `Request` | `SignUpRequest`, `LoginRequest` | HTTP 요청 (Web 계층) |
| **Response DTO** | `Response` | `SignUpResponse`, `LoginResponse` | HTTP 응답 (Web 계층) |
| **Domain Event** | `Event` | `OrderMatchedEvent`, `StockListedEvent` | Kafka 이벤트 |
| **Exception** | `Exception` | `BusinessException`, `ResourceNotFoundException` | 예외 클래스 |

### 2.2 UseCase 인터페이스 네이밍

```
동사 + 대상 + UseCase
```

| 패턴 | 예시 |
|------|------|
| 생성 | `CreateStockUseCase`, `SignUpUseCase` |
| 조회 (단건) | `GetStockDetailUseCase`, `GetCurrentUserUseCase` |
| 조회 (목록) | `GetStockListUseCase`, `GetOrderBookUseCase` |
| 검색 | `SearchStockUseCase` |
| 수정 | `UpdateUserUseCase` |
| 삭제 | `DelistStockUseCase`, `CancelOrderUseCase` |
| 실행 | `PlaceOrderUseCase`, `LoginUseCase` |
| 존재 확인 | `CheckStockExistsUseCase` |

### 2.3 Port 인터페이스 네이밍

```
대상 + 역할 + Port
```

| 패턴 | 예시 |
|------|------|
| 영속성 | `UserPersistencePort`, `OrderPersistencePort` |
| 이벤트 발행 | `TradingEventPort`, `StockEventPublishPort` |
| 캐시 | `OrderBookCachePort` |
| 외부 조회 | `StockExistenceCheckPort`, `StockQueryPort` |

### 2.4 메서드 네이밍

**UseCase / Handler:**

| 작업 | 메서드명 | 파라미터 | 반환 |
|------|----------|----------|------|
| 생성 | `createStock(command)` | `CreateStockCommand` | `StockDetailResult` |
| 조회 단건 | `getStockDetail(stockId)` | `String` | `StockDetailResult` |
| 조회 목록 | `getStockList(query)` | `StockListQuery` | `Page<StockListItemResult>` |
| 수정 | `updateUsername(command)` | `UpdateCommand` | `UserResult` |
| 삭제 | `cancelOrder(command)` | `CancelOrderCommand` | `Unit` |
| 실행 | `placeOrder(command)` | `PlaceOrderCommand` | `PlaceOrderResult` |

**Persistence Port:**

| 작업 | 메서드명 |
|------|----------|
| 저장 | `save(model): Model` |
| ID 조회 | `findById(id): Model?` |
| 조건 조회 | `findByEmail(email): Model?` |
| 필터 조회 | `findByFilters(..., pageable): Page<Model>` |
| 수정 | `update(model): Model` |
| 존재 확인 | `existsByStockName(name): Boolean` |

**Domain Model (팩토리 메서드):**

| 용도 | 메서드명 | 설명 |
|------|----------|------|
| 신규 생성 | `Model.create(...)` | ID 없이 새 인스턴스 생성 |
| DB 재구성 | `Model.of(...)` | 기존 데이터로 인스턴스 복원 |
| 상태 변경 | `model.fill(qty)`, `model.cancel()` | 불변 copy 반환 |

**JPA Entity (변환 메서드):**

| 용도 | 메서드명 | 방향 |
|------|----------|------|
| Entity → Domain | `entity.toDomain()` | 인스턴스 메서드 |
| Domain → Entity | `Entity.fromDomain(model)` | companion 팩토리 |
| 부분 업데이트 | `entity.updateFromDomain(model)` | 인스턴스 메서드 |

**Result DTO (변환 메서드):**

| 용도 | 메서드명 |
|------|----------|
| Domain → Result | `Result.from(model)` |

### 2.5 Enum 네이밍

```kotlin
enum class 이름(val displayName: String) {
    UPPER_SNAKE("한글 표시명")
}
```

```kotlin
// 예시
enum class OrderStatus(val displayName: String) {
    PENDING("대기중"),
    PARTIALLY_FILLED("부분체결"),
    FILLED("체결완료"),
    CANCELLED("취소됨"),
    REJECTED("거부됨")
}
```

### 2.6 Kafka 토픽 네이밍

```
{도메인}.{과거분사}
```

| 패턴 | 예시 |
|------|------|
| 생성 | `order.created`, `user.created`, `investor.created` |
| 변경 | `price.updated`, `orderbook.updated`, `ranking.updated` |
| 완료 | `order.matched`, `trade.executed` |
| 삭제 | `order.cancelled`, `stock.delisted` |
| 발생 | `event.occurred`, `news.published`, `stock.listed` |

### 2.7 ErrorCode 네이밍

```
{도메인 접두사}{3자리 숫자}
```

| 접두사 | 도메인 | 예시 |
|--------|--------|------|
| `C` | Common | `C001` (잘못된 입력), `C002` (리소스 없음) |
| `A` | Auth | `A001` (인증 필요), `A003` (유효하지 않은 토큰) |
| `U` | User | `U001` (사용자 없음), `U002` (이메일 중복) |
| `S` | Stock | `S001` (종목 없음), `S002` (장 마감) |
| `T` | Trading | `T001` (잔액 부족), `T005` (주문 없음) |
| `E` | Event | `E001` (이벤트 없음) |
| `N` | News | `N001` (뉴스 없음) |
| `SC` | Scheduler | `SC001` (스케줄러 없음) |

---

## 3. 공통 모듈 (common)

### 3.1 API 응답 래퍼

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T>
        fun <T> success(message: String? = null): ApiResponse<T>       // data 없는 성공
        fun <T> error(message: String, errorCode: String? = null): ApiResponse<T>
        fun <T> created(data: T, message: String? = null): ApiResponse<T>
    }
}

// 확장 함수
fun <T> ApiResponse<T>.toResponseEntity(): ResponseEntity<ApiResponse<T>>           // 200 OK
fun <T> ApiResponse<T>.toResponseEntity(status: HttpStatus): ResponseEntity<...>    // 커스텀 상태
fun <T> ApiResponse<T>.toCreatedResponseEntity(): ResponseEntity<...>               // 201 Created
```

**성공 응답:**
```json
{
  "success": true,
  "data": { ... },
  "message": "회원가입이 완료되었습니다",
  "errorCode": null,
  "timestamp": 1707284956000
}
```

**에러 응답:**
```json
{
  "success": false,
  "data": null,
  "message": "리소스를 찾을 수 없습니다",
  "errorCode": "C002",
  "timestamp": 1707284956000
}
```

### 3.2 페이징 응답

```kotlin
@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
) {
    companion object {
        fun <T> of(content: List<T>, page: Int, size: Int, totalElements: Long): PageResponse<T>
    }
}

// 무한 스크롤용
@Serializable
data class SliceResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val first: Boolean,
    val empty: Boolean
)
```

### 3.3 예외 체계

```kotlin
// 기본 비즈니스 예외
open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)

// 특화 예외 (용도별 분리)
class ResourceNotFoundException(errorCode, message)       // 404
class UnauthorizedException(errorCode, message)           // 401
class ForbiddenException(errorCode, message)              // 403
class InvalidInputException(errorCode, message)           // 400
class DuplicateResourceException(errorCode, message)      // 409
class InsufficientResourceException(errorCode, message)   // 400
```

```kotlin
// ErrorCode Enum
enum class ErrorCode(
    val code: String,          // "C001", "U002" 등
    val message: String,       // 기본 에러 메시지
    val httpStatus: HttpStatus // HTTP 상태 코드
) {
    // Common (C0xx)
    INVALID_INPUT("C001", "잘못된 입력입니다", BAD_REQUEST),
    RESOURCE_NOT_FOUND("C002", "리소스를 찾을 수 없습니다", NOT_FOUND),
    INTERNAL_ERROR("C003", "서버 내부 오류가 발생했습니다", INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("C004", "유효성 검사에 실패했습니다", BAD_REQUEST),

    // Auth (A0xx)
    UNAUTHORIZED("A001", "인증이 필요합니다", UNAUTHORIZED),
    FORBIDDEN("A002", "접근 권한이 없습니다", FORBIDDEN),

    // {도메인별 추가}
    // ...
}
```

### 3.4 글로벌 예외 핸들러

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn { "BusinessException: [${ex.errorCode.code}] ${ex.message}" }
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(ApiResponse.error(ex.message, ex.errorCode.code))
    }

    @ExceptionHandler(WebExchangeBindException::class)        // Validation 에러
    fun handleValidationException(ex): ResponseEntity<...>

    @ExceptionHandler(IllegalArgumentException::class)        // require() 실패
    fun handleIllegalArgumentException(ex): ResponseEntity<...>

    @ExceptionHandler(Exception::class)                       // 기타 모든 예외
    fun handleException(ex): ResponseEntity<...>
}
```

### 3.5 도메인 이벤트

```kotlin
// 이벤트 베이스 클래스
abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now()
) {
    abstract val eventType: String
}

// 토픽 상수 (object)
object KafkaTopics {
    const val ORDER_CREATED = "order.created"
    const val ORDER_MATCHED = "order.matched"
    // ...
}

// 이벤트 구현체
data class OrderMatchedEvent(
    val tradeId: String,
    val buyOrderId: String,
    val sellOrderId: String,
    val price: Long,
    val quantity: Long,
    val matchedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "ORDER_MATCHED"
}
```

### 3.6 Kafka 공통 설정

```kotlin
@EnableKafka
@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    // Producer: Jackson JSON, acks=all, retries=3, idempotent
    @Bean fun producerFactory(): ProducerFactory<String, Any>
    @Bean fun kafkaTemplate(): KafkaTemplate<String, Any>

    // Consumer: Jackson JSON, manual commit, concurrency=3
    @Bean fun consumerFactory(): ConsumerFactory<String, Any>
    @Bean fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any>

    // Topics: partitions=3, replicas=1
    @Bean fun orderCreatedTopic(): NewTopic = createTopic(KafkaTopics.ORDER_CREATED)

    private fun createTopic(name: String, partitions: Int = 3, replicas: Int = 1): NewTopic
}
```

---

## 4. 계층별 구현 패턴

### 4.1 Domain Model

```kotlin
data class OrderModel(
    val orderId: String,             // 불변 식별자
    val userId: String,              // 불변 참조
    val quantity: Long,              // 불변 속성
    val filledQuantity: Long,        // 상태 값
    val status: OrderStatus,         // 상태 값
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    // ── 파생 속성 ──
    val remainingQuantity: Long
        get() = quantity - filledQuantity

    // ── 생성 시 검증 (init block) ──
    init {
        require(orderId.isNotBlank()) { "orderId must not be blank" }
        require(quantity > 0) { "quantity must be positive" }
        require(filledQuantity >= 0) { "filledQuantity must not be negative" }
        require(filledQuantity <= quantity) { "filledQuantity must not exceed quantity" }
    }

    // ── 상태 변경 메서드 (불변 copy 반환) ──
    fun fill(matchedQuantity: Long): OrderModel {
        require(matchedQuantity > 0) { "matchedQuantity must be positive" }
        val newFilled = filledQuantity + matchedQuantity
        val newStatus = if (newFilled == quantity) OrderStatus.FILLED else OrderStatus.PARTIALLY_FILLED
        return copy(filledQuantity = newFilled, status = newStatus, updatedAt = Instant.now())
    }

    fun cancel(): OrderModel {
        require(status == OrderStatus.PENDING || status == OrderStatus.PARTIALLY_FILLED)
        return copy(status = OrderStatus.CANCELLED, updatedAt = Instant.now())
    }

    // ── 팩토리 메서드 (companion object) ──
    companion object {
        /** 신규 생성 (ID 자동 발급) */
        fun create(userId: String, quantity: Long, ...): OrderModel {
            return OrderModel(
                orderId = UUID.randomUUID().toString(),
                userId = userId,
                quantity = quantity,
                filledQuantity = 0,
                status = OrderStatus.PENDING
            )
        }

        /** 기존 데이터 복원 (DB 조회 후) */
        fun of(orderId: String, userId: String, ...): OrderModel {
            return OrderModel(orderId = orderId, userId = userId, ...)
        }
    }
}
```

**핵심 규칙:**
- 프레임워크 의존 없음 (순수 Kotlin)
- `data class` 사용, `copy()`로 불변 상태 변경
- `init` 블록에서 invariant 검증 (`require`)
- `create()`: 신규 생성 (ID null 또는 UUID 자동), `of()`: DB 재구성

### 4.2 UseCase Interface (In Port)

```kotlin
// 파일: application/port/in/{aggregate}/{동사}{대상}UseCase.kt

interface PlaceOrderUseCase {
    fun placeOrder(command: PlaceOrderCommand): PlaceOrderResult
}

interface GetStockListUseCase {
    fun getStockList(query: StockListQuery): Page<StockListItemResult>
}

interface CancelOrderUseCase {
    fun cancelOrder(command: CancelOrderCommand)    // 반환값 없음 가능
}
```

**핵심 규칙:**
- 인터페이스 1개 = 메서드 1개 (Single Responsibility)
- Command/Query DTO를 파라미터로 받음
- Result DTO를 반환 (도메인 모델 직접 노출 금지)

### 4.3 Out Port Interface

```kotlin
// 파일: application/port/out/{aggregate}/{대상}{역할}Port.kt

interface OrderPersistencePort {
    fun save(order: OrderModel): OrderModel
    fun findById(orderId: String): OrderModel?
    fun update(order: OrderModel): OrderModel
    fun findByStockIdAndStatusIn(stockId: String, statuses: List<OrderStatus>): List<OrderModel>
}

interface TradingEventPort {
    fun publishOrderMatched(matchResult: MatchResultVo)
    fun publishOrderCancelled(orderId: String, userId: String, stockId: String, reason: String)
    fun publishOrderBookUpdated(snapshot: OrderBookSnapshotVo)
}

interface OrderBookCachePort {
    fun saveSnapshot(stockId: String, snapshot: OrderBookSnapshotVo)
    fun loadEntries(stockId: String, side: OrderType): List<OrderEntryVo>
    fun saveEntries(stockId: String, entries: List<OrderEntryVo>, side: OrderType)
}
```

**핵심 규칙:**
- 도메인 모델로 입출력 (JPA Entity 노출 금지)
- 반환 nullable (`Model?`) → 호출자가 예외 처리 결정

### 4.4 Command / Query / Result DTO

```kotlin
// ── Command (쓰기 요청) ──
// 파일: application/dto/command/{aggregate}/{동사}{대상}Command.kt

data class PlaceOrderCommand(
    val userId: String,
    val stockId: String,
    val orderType: OrderType,
    val price: Long?,
    val quantity: Long
)
// 검증 없음. 순수 데이터 전달 객체.

// ── Query (읽기 요청) ──
// 파일: application/dto/query/{aggregate}/{대상}Query.kt

data class StockListQuery(
    val page: Int = 0,
    val size: Int = 20,
    val sector: String? = null,
    val sortBy: String = "stockName"
)

// ── Result (응답) ──
// 파일: application/dto/result/{aggregate}/{대상}Result.kt

data class StockDetailResult(
    val stockId: String,
    val stockName: String,
    val currentPrice: Long,
    val status: StockStatus
) {
    companion object {
        fun from(stock: StockModel): StockDetailResult = StockDetailResult(
            stockId = stock.stockId,
            stockName = stock.stockName,
            currentPrice = stock.currentPrice,
            status = stock.status
        )
    }
}
```

### 4.5 Handler (UseCase 구현체)

```kotlin
// 파일: application/handler/{aggregate}/{대상}CommandHandler.kt

@Service
class OrderCommandHandler(
    private val orderPersistencePort: OrderPersistencePort,      // Out Port 주입
    private val tradingEventPort: TradingEventPort,
    private val orderBookRegistry: OrderBookRegistry             // Domain Service
) : PlaceOrderUseCase, CancelOrderUseCase {                     // In Port 구현

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun placeOrder(command: PlaceOrderCommand): PlaceOrderResult {
        // 1. 비즈니스 검증
        if (command.orderKind == OrderKind.LIMIT) {
            if (!PriceUtil.isValidPrice(command.price!!)) {
                throw InvalidInputException(ErrorCode.INVALID_ORDER_PRICE, "유효하지 않은 호가 단위입니다")
            }
        }

        // 2. 도메인 모델 생성
        val order = OrderModel.create(
            userId = command.userId,
            stockId = command.stockId,
            orderType = command.orderType,
            price = command.price,
            quantity = command.quantity
        )

        // 3. 영속화
        orderPersistencePort.save(order)

        // 4. 도메인 서비스 호출
        val matches = orderBookRegistry.placeOrder(...)

        // 5. 상태 변경 & 업데이트
        var updatedOrder = order
        if (matches.isNotEmpty()) {
            updatedOrder = updatedOrder.fill(totalFilledQuantity)
            orderPersistencePort.update(updatedOrder)
        }

        // 6. 이벤트 발행
        matches.forEach { tradingEventPort.publishOrderMatched(it) }

        // 7. Result 반환
        log.info("주문 접수 완료: orderId={}", order.orderId)
        return PlaceOrderResult.from(updatedOrder, matches)
    }
}
```

**핵심 규칙:**
- `@Service` + `@Transactional` (쓰기 작업)
- 여러 UseCase 인터페이스 구현 가능 (같은 Aggregate)
- Port 인터페이스로만 의존 (구현체 직접 참조 금지)
- 순서: 검증 → 생성 → 영속화 → 도메인 로직 → 상태 업데이트 → 이벤트 발행 → 결과 반환
- 번호 주석으로 단계 구분

### 4.6 Web Adapter (REST Controller)

```kotlin
// 파일: infrastructure/adapter/in/web/{대상}WebAdapter.kt

@RestController
@RequestMapping("/api/v1/{domain}")
@Tag(name = "대상", description = "대상 API")
class UserWebAdapter(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest
    ): Mono<ResponseEntity<ApiResponse<SignUpResponse>>> = mono {

        // 1. Request → Command 변환
        val command = SignUpCommand(
            email = request.email,
            username = request.username,
            password = request.password
        )

        // 2. UseCase 호출
        val result = signUpUseCase.signUp(command)

        // 3. Result → Response 변환
        val response = SignUpResponse(
            userId = result.userId,
            email = result.email,
            username = result.username
        )

        // 4. ApiResponse 래핑
        ApiResponse.created(response, "회원가입이 완료되었습니다").toCreatedResponseEntity()
    }

    @GetMapping("/{id}")
    @Operation(summary = "단건 조회")
    fun getDetail(
        @Parameter(description = "ID") @PathVariable id: String
    ): Mono<ResponseEntity<ApiResponse<DetailResponse>>> = mono {
        val result = getDetailUseCase.getDetail(id)
        ApiResponse.success(DetailResponse.from(result)).toResponseEntity()
    }
}
```

**핵심 규칙:**
- `@RestController` + `@RequestMapping("/api/v1/{domain}")`
- `@Tag`, `@Operation` 으로 Swagger 문서화
- `Mono<ResponseEntity<ApiResponse<T>>>` 반환 (WebFlux)
- `mono { }` 코루틴 빌더로 감싸기
- 데이터 변환 파이프라인: `Request → Command → [UseCase] → Result → Response`
- Web 계층의 DTO (`Request`/`Response`)와 Application 계층 DTO (`Command`/`Result`) 분리

### 4.7 Request / Response DTO (Web 계층)

```kotlin
// 파일: infrastructure/adapter/in/web/dto/{대상}Request.kt

@Schema(description = "회원가입 요청")
data class SignUpRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    @Schema(description = "이메일", example = "user@example.com", required = true)
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]+\$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    @Schema(description = "비밀번호", example = "Test1234!", required = true)
    val password: String
)
```

**핵심 규칙:**
- `@field:` prefix 필수 (Kotlin data class에서 Jakarta Validation 사용 시)
- `@Schema` 로 Swagger 문서화 (description, example, required)
- 검증은 Request DTO에서만 (Command DTO에는 검증 없음)

### 4.8 JPA Entity

```kotlin
// 파일: infrastructure/adapter/out/persistence/{aggregate}/entity/{대상}JpaEntity.kt

@Entity
@Table(
    name = "users",
    schema = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email")
    ]
)
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(nullable = false, length = 255)
    var password: String,              // 변경 가능한 필드는 var

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val role: UserRole = UserRole.ROLE_USER,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    // ── JPA 기본 생성자 ──
    protected constructor() : this(
        userId = null, email = "", password = "", username = ""
    )

    // ── Entity → Domain ──
    fun toDomain(): UserModel = UserModel.of(
        userId = userId ?: throw IllegalStateException("User ID must not be null"),
        email = email,
        password = password,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // ── Domain → Entity ──
    companion object {
        fun fromDomain(user: UserModel, encodedPassword: String): UserJpaEntity {
            return UserJpaEntity(
                userId = user.userId,
                email = user.email,
                password = encodedPassword,
                role = user.role,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }

    // ── 부분 업데이트 (기존 Entity 재사용) ──
    fun updateFromDomain(domain: DomainModel) {
        this.price = domain.price
        this.status = domain.status
        this.updatedAt = Instant.now()
    }

    // ── equals/hashCode (ID 기반) ──
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserJpaEntity
        return userId != null && userId == other.userId
    }

    override fun hashCode(): Int = userId?.hashCode() ?: 0
}
```

**핵심 규칙:**
- `class` (NOT `data class`) — JPA 프록시 호환
- `protected constructor()` — Hibernate 기본 생성자
- `val` (불변) vs `var` (변경 가능) 명확히 구분
- `@Enumerated(EnumType.STRING)` — DB에 문자열로 저장
- `equals()`/`hashCode()` — ID 기반, null 안전
- 스키마 분리: `schema = "users"`, `schema = "trading"` 등
- `toDomain()` / `fromDomain()` / `updateFromDomain()` 3종 세트

### 4.9 Persistence Adapter

```kotlin
// 파일: infrastructure/adapter/out/persistence/{aggregate}/adapter/{대상}PersistenceAdapter.kt

@Component
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserPersistencePort {

    override fun findByEmail(email: String): UserModel? {
        return userJpaRepository.findByEmail(email)?.toDomain()
    }

    override fun save(user: UserModel): UserModel {
        return userJpaRepository.save(UserJpaEntity.fromDomain(user, user.password)).toDomain()
    }

    override fun update(user: UserModel): UserModel {
        val entity = userJpaRepository.findById(user.userId!!)
            .orElseThrow { ResourceNotFoundException(ErrorCode.USER_NOT_FOUND) }
        entity.updateFromDomain(user)
        return userJpaRepository.save(entity).toDomain()
    }

    override fun findByFilters(..., pageable: Pageable): Page<UserModel> {
        return userJpaRepository.findByFilters(..., pageable).map { it.toDomain() }
    }
}
```

**핵심 규칙:**
- `@Component` + Port 인터페이스 구현
- JPA Repository 위임 + `toDomain()` / `fromDomain()` 변환
- `Page` 결과도 `.map { it.toDomain() }` 으로 변환
- JPA Entity가 도메인 계층에 노출되지 않음

### 4.10 Kafka Consumer (Event In Adapter)

```kotlin
// 파일: infrastructure/adapter/in/event/{대상}EventConsumer.kt

@Component
class StockEventConsumer(
    private val createStockUseCase: CreateStockUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.STOCK_LISTED], groupId = "stock-service")
    fun handleStockListed(event: StockListedEvent, ack: Acknowledgment) {
        try {
            log.info("Received stock.listed event: stockId={}", event.stockId)

            // Event → Command 변환
            val command = CreateStockCommand(
                stockId = event.stockId,
                stockName = event.stockName,
                sector = Sector.valueOf(event.sector)
            )

            // UseCase 호출
            createStockUseCase.createStock(command)

            log.info("Stock listed successfully: {}", event.stockId)
            ack.acknowledge()    // 수동 커밋
        } catch (e: Exception) {
            log.error("Failed to handle stock.listed event: stockId={}", event.stockId, e)
            ack.acknowledge()    // Poison pill 방지 (DLQ 사용 시 nack)
        }
    }
}
```

**핵심 규칙:**
- `@KafkaListener(topics, groupId)` — groupId는 서비스명
- 수동 커밋 (`Acknowledgment.acknowledge()`)
- `Event → Command` 변환 후 UseCase 호출
- try-catch + ack로 Poison pill 방지
- 에러 시에도 acknowledge (무한 재시도 방지)

### 4.11 Kafka Producer (Event Out Adapter)

```kotlin
// 파일: infrastructure/adapter/out/event/Kafka{도메인}EventPublisher.kt

@Component
class KafkaTradingEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : TradingEventPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishOrderMatched(matchResult: MatchResultVo) {
        val event = OrderMatchedEvent(
            tradeId = matchResult.tradeId,
            buyOrderId = matchResult.buyOrderId,
            price = matchResult.price,
            quantity = matchResult.quantity
        )
        kafkaTemplate.send(KafkaTopics.ORDER_MATCHED, matchResult.stockId, event)
        log.debug("주문 체결 이벤트 발행: tradeId={}", matchResult.tradeId)
    }
}
```

**핵심 규칙:**
- `TradingEventPort` 구현
- Domain VO → Event DTO 변환
- `kafkaTemplate.send(topic, key, event)` — key는 파티셔닝 기준 (보통 ID)
- debug 레벨 로깅

### 4.12 Redis Cache Adapter

```kotlin
// 파일: infrastructure/adapter/out/cache/Redis{대상}CacheAdapter.kt

@Component
class RedisOrderBookCacheAdapter(
    private val redissonClient: RedissonClient
) : OrderBookCachePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveSnapshot(stockId: String, snapshot: OrderBookSnapshotVo) {
        try {
            val bucket = redissonClient.getBucket<Map<String, Any>>("orderbook:$stockId:snapshot")
            bucket.set(mapOf(
                "stockId" to snapshot.stockId,
                "bestBid" to (snapshot.bestBid?.toString() ?: ""),
                "bestAsk" to (snapshot.bestAsk?.toString() ?: "")
            ))
        } catch (e: Exception) {
            log.warn("Redis 스냅샷 저장 실패: stockId={}, 원인={}", stockId, e.message)
        }
    }

    override fun loadEntries(stockId: String, side: OrderType): List<OrderEntryVo> {
        try {
            val map = redissonClient.getMap<String, String>("orderbook:$stockId:$sideName")
            return map.entries.mapNotNull { (orderId, value) ->
                // 파싱 로직
            }
        } catch (e: Exception) {
            log.warn("Redis 엔트리 로드 실패: stockId={}", stockId)
            return emptyList()    // Graceful degradation
        }
    }
}
```

**핵심 규칙:**
- Redis Key 패턴: `{도메인}:{id}:{세부항목}`
- try-catch + 경고 로그 → **Graceful degradation** (캐시 실패 시 빈 결과 반환)
- 캐시 어댑터는 절대 예외를 던지지 않음

### 4.13 Feign Client (외부 서비스 호출)

```kotlin
// 파일: infrastructure/adapter/out/client/{서비스}FeignClient.kt

@FeignClient(name = "stock-service")
interface StockServiceFeignClient {
    @GetMapping("/api/stocks/exists")
    fun checkStockExists(
        @RequestParam(required = false) stockId: String? = null,
        @RequestParam(required = false) stockName: String? = null
    ): ApiResponse<StockExistsResponse>
}

// Adapter (Port 구현)
@Component
class StockExistenceCheckAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : StockExistenceCheckPort {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun existsByStockName(name: String): Boolean {
        return try {
            val response = stockServiceFeignClient.checkStockExists(stockName = name)
            response.data?.exists ?: false
        } catch (e: Exception) {
            log.warn("Stock existence check failed: name={}", name)
            false    // 실패 시 false 반환 (안전한 기본값)
        }
    }
}
```

**핵심 규칙:**
- `@FeignClient(name = "서비스명")` — Eureka 서비스 디스커버리
- Adapter 클래스에서 Port 구현
- 실패 시 안전한 기본값 반환 (Circuit Breaker 패턴)

---

## 5. 데이터 흐름 패턴

### 5.1 쓰기 (Command) 흐름

```
[Client]
    ↓ HTTP POST/PUT/DELETE
[WebAdapter]
    ↓ @Valid 검증
    ↓ Request → Command 변환
[Handler] (@Transactional)
    ↓ 비즈니스 검증
    ↓ DomainModel.create() / model.상태변경()
    ↓ PersistencePort.save() / .update()
    ↓ EventPort.publish*()
    ↓ Result DTO 생성
[WebAdapter]
    ↓ Result → Response 변환
    ↓ ApiResponse.success() / .created()
[Client]
    ← ResponseEntity<ApiResponse<Response>>
```

### 5.2 읽기 (Query) 흐름

```
[Client]
    ↓ HTTP GET
[WebAdapter]
    ↓ @RequestParam → Query 변환
[Handler]
    ↓ PersistencePort.findBy*()
    ↓ Result.from(model)
[WebAdapter]
    ↓ Response.from(result) 또는 직접 매핑
    ↓ ApiResponse.success(PageResponse.of(...))
[Client]
    ← ResponseEntity<ApiResponse<PageResponse<Response>>>
```

### 5.3 이벤트 (Event) 흐름

```
[Producer Service]
    ↓ Handler에서 비즈니스 로직 완료 후
    ↓ EventPort.publish*(domainVO)
[KafkaEventPublisher]
    ↓ Domain VO → Event DTO 변환
    ↓ kafkaTemplate.send(topic, key, event)
[Kafka]
    ↓ 메시지 전달
[Consumer Service]
[EventConsumer]
    ↓ @KafkaListener
    ↓ Event → Command 변환
    ↓ UseCase.handle(command)
    ↓ ack.acknowledge()
```

---

## 6. 검증 전략 (4단계)

| 단계 | 위치 | 방법 | 검증 대상 |
|------|------|------|-----------|
| 1. **입력 검증** | Request DTO | `@field:NotBlank`, `@field:Email`, `@Valid` | 형식, 필수값, 길이 |
| 2. **비즈니스 검증** | Handler | `throw BusinessException(ErrorCode.*)` | 중복, 권한, 상태 전이 |
| 3. **도메인 불변식** | Domain Model `init` | `require(...)` | 데이터 무결성 |
| 4. **상태 검증** | Domain Model 메서드 | `require(...)` | 상태 전이 규칙 |

---

## 7. DB 스키마 컨벤션

### 7.1 스키마 분리

```sql
-- 서비스별 독립 스키마
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS stocks;
CREATE SCHEMA IF NOT EXISTS trading;
CREATE SCHEMA IF NOT EXISTS event;
CREATE SCHEMA IF NOT EXISTS news;
```

### 7.2 테이블 네이밍

```sql
CREATE TABLE {schema}.{복수형_테이블명} (
    {테이블_단수형}_id    {타입}   PRIMARY KEY,    -- PK: 테이블명_id
    created_at          TIMESTAMP DEFAULT NOW(),  -- 생성일시
    updated_at          TIMESTAMP DEFAULT NOW()   -- 수정일시
);

-- 인덱스: idx_{테이블}_{컬럼}
CREATE INDEX idx_orders_user_id ON trading.orders(user_id);
CREATE INDEX idx_orders_status ON trading.orders(status);
```

### 7.3 Flyway 마이그레이션

```
src/main/resources/db/migration/
├── V1__create_{도메인}_table.sql
├── V2__add_{컬럼}_to_{테이블}.sql
└── V3__create_{인덱스}_index.sql
```

---

## 8. 분산 락 패턴 (Redisson)

```kotlin
@Component
class OrderBookRegistry(
    private val redissonClient: RedissonClient
) {
    private val orderBooks = ConcurrentHashMap<String, OrderBookDomainService>()

    fun placeOrder(stockId: String, ...): List<MatchResultVo> =
        withStockLock(stockId) {
            val orderBook = orderBooks.getOrPut(stockId) { OrderBookDomainService(stockId) }
            orderBook.addOrder(entry, orderType, orderKind)
        }

    private fun <T> withStockLock(stockId: String, action: () -> T): T {
        val lock = redissonClient.getLock("lock:orderbook:$stockId")
        val acquired = lock.tryLock(10, 30, TimeUnit.SECONDS)    // wait 10s, lease 30s
        if (!acquired) throw IllegalStateException("Failed to acquire lock for stock: $stockId")
        try {
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) lock.unlock()
        }
    }
}
```

**락 Key 패턴:** `lock:{도메인}:{id}`

---

## 9. Gradle 공통 설정

### 9.1 루트 `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    kotlin("plugin.jpa") version "2.3.0" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}
```

### 9.2 `backend/build.gradle.kts` (subprojects 공통)

```kotlin
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    the<DependencyManagementExtension>().apply {
        imports { mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1") }
    }

    dependencies {
        // Kotlin
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
        "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")

        // Jackson + Logging
        "implementation"("tools.jackson.module:jackson-module-kotlin")
        "implementation"("io.github.microutils:kotlin-logging-jvm:3.0.5")

        // Test
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("io.mockk:mockk:1.13.12")
        "testImplementation"("com.ninja-squad:springmockk:4.0.2")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }
}
```

---

## 10. 설정 파일 패턴

### 10.1 이중 프로필 (Local + Docker)

```
src/main/resources/
├── application.yml              # 로컬 개발 (localhost)
└── application-docker.yml       # Docker 환경 (환경변수)
```

```yaml
# application.yml (로컬)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stocksim
    username: stocksim
    password: stocksim123

# application-docker.yml (Docker)
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST}:5432/stocksim
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
```

---

## 11. 로깅 전략

| 레벨 | 용도 | 예시 |
|------|------|------|
| `log.debug` | 세부 추적 (이벤트 발행, 캐시 저장) | `"이벤트 발행: tradeId={}", id` |
| `log.info` | 비즈니스 이벤트 (성공) | `"주문 접수 완료: orderId={}", id` |
| `log.warn` | 비즈니스 예외, 캐시 실패 | `"BusinessException: [{}] {}", code, msg` |
| `log.error` | 시스템 오류, 예상치 못한 예외 | `"Unexpected error occurred", ex` |

```kotlin
// 로거 선언 (두 가지 방식)
private val log = LoggerFactory.getLogger(javaClass)              // SLF4J
private val logger = KotlinLogging.logger {}                      // kotlin-logging

// 로깅 패턴
log.info("주문 접수 완료: orderId={}, 상태={}, 체결건수={}", order.orderId, status, matches.size)
logger.warn { "BusinessException: [${ex.errorCode.code}] ${ex.message}" }
```

---

## 12. 요약: 새 서비스 생성 체크리스트

1. **모듈 생성**: `backend/{domain}-service/` 디렉토리 + `build.gradle.kts`
2. **settings.gradle.kts**: `include("backend:{domain}-service")` 추가
3. **패키지 구조**: 섹션 1.2 의 구조 그대로 생성
4. **Domain Model**: `create()` + `of()` 팩토리, `init` 검증, 불변 `copy()` 상태 변경
5. **UseCase 인터페이스**: 1 interface = 1 method
6. **Port 인터페이스**: Persistence / Event / Cache 분리
7. **Handler**: `@Service` + `@Transactional`, UseCase 구현
8. **DTO**: Command / Query / Result (Application) + Request / Response (Web)
9. **WebAdapter**: `@RestController`, `Mono` 반환, `@Valid` 검증
10. **JPA Entity**: `toDomain()` / `fromDomain()` / `updateFromDomain()`
11. **Persistence Adapter**: Port 구현, 도메인 변환
12. **Kafka**: Consumer (`@KafkaListener` + 수동 ack) / Producer (Port 구현)
13. **Flyway**: `V1__create_{domain}_table.sql`
14. **ErrorCode**: `{prefix}{3자리}` 추가
15. **application.yml** / **application-docker.yml** 생성
16. **docker-compose.yml**: 서비스 추가
17. **API Gateway**: `RouteConfig.kt` 에 라우트 추가
