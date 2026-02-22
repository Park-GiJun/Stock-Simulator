# NPC/기관 자동 매수/매도 시스템 구현 가이드

## 개요

NPC/기관 투자자가 주기적으로 자동 매수/매도를 수행하여 호가창에 유동성을 공급한다.

**핵심 원칙:**
- 스케줄러는 **scheduler-service**에서만 실행
- 매매 의사결정 로직은 **trading-service**에서 Kafka 이벤트 수신 후 처리
- 각 NPC/기관별로 **코루틴으로 개별 실행**
- 기존 DB 필드(`trading.investor_balances`, `trading.portfolios`, `trading.orders`)를 최대한 활용

---

## 아키텍처 흐름

```
scheduler-service                    trading-service
┌──────────────────────┐            ┌──────────────────────────────────┐
│ AutoTradingScheduler │            │ TradingEventConsumer             │
│                      │            │   └ handleAutoTradeSignal()      │
│ @Scheduled(5분/15분) │──Kafka──→ │       └ AutoTradingHandler       │
│ "auto.trade.signal"  │            │           ├ NPC 1  (coroutine)  │
│                      │            │           ├ NPC 2  (coroutine)  │
│ 이벤트 발행만 담당    │            │           ├ ...                  │
│ (로직 없음)          │            │           └ INST 5 (coroutine)  │
│                      │            │                                  │
│                      │            │   기존 코드 재사용:              │
│                      │            │   - PlaceOrderUseCase.placeOrder │
│                      │            │   - OrderBookRegistry.getSnapshot│
│                      │            │   - PriceUtil                    │
└──────────────────────┘            └──────────────────────────────────┘
```

---

## 현재 사용 가능한 기존 코드/DB

### DB 테이블 (이미 존재, V3 마이그레이션)

| 테이블 | 용도 | 비고 |
|--------|------|------|
| `trading.investor_balances` | 투자자 잔고 | `investor_id`, `investor_type`, `balance` |
| `trading.portfolios` | 보유 포트폴리오 | `investor_id`, `investor_type`, `stock_id`, `quantity`, `average_price` |
| `trading.orders` | 주문 | `investor_type` 필드로 NPC/INSTITUTION 구분 |
| `trading.trades` | 체결 이력 | 체결가, 수량 확인용 |

### 기존 코드

| 코드 | 위치 | 재사용 방법 |
|------|------|-------------|
| `PlaceOrderUseCase.placeOrder()` | `OrderCommandHandler.kt` | 주문 실행 (그대로 호출) |
| `OrderBookRegistry.getSnapshot()` | `OrderBookRegistry.kt` | bestBid/bestAsk 조회 |
| `PriceUtil.adjustPriceUp/Down()` | `PriceUtil.kt` (common) | 호가 단위 조정 |
| `PriceUtil.getTickSize()` | `PriceUtil.kt` (common) | 호가 단위 계산 |
| `SystemConstants.SYSTEM_INSTITUTIONS` | `SystemConstants.kt` | 기관 ID 목록 (5개) |
| `SystemConstants.SYSTEM_NPCS` | `SystemConstants.kt` | NPC ID 목록 (10개) |
| `SystemConstants.NPC_SECTOR_PREFERENCES` | `SystemConstants.kt` | NPC별 관심 섹터 |
| `PlaceOrderCommand` | `PlaceOrderCommand.kt` | userId, stockId, orderType, orderKind, price, quantity |
| `ScheduleTradeEvent` | `DomainEvent.kt` (common) | 이미 정의됨 (그대로 쓸 수도 있음) |
| `KafkaTopics.SCHEDULE_TRADE` | `DomainEvent.kt` (common) | 이미 정의됨 |
| `InvestmentStyle` enum | `Enums.kt` (common) | AGGRESSIVE, STABLE, VALUE, RANDOM |
| `OrderBookSnapshotVo` | `OrderBookSnapshotVo.kt` | bestBid, bestAsk, spread |

---

## 구현 순서

### Step 1: common 모듈 - Kafka 이벤트 추가

**파일: `backend/common/src/main/kotlin/.../event/DomainEvent.kt`**

`KafkaTopics`에 새 토픽 추가:
```kotlin
object KafkaTopics {
    // ... 기존 토픽들 ...

    // Auto Trading 관련
    const val AUTO_TRADE_NPC = "auto.trade.npc"
    const val AUTO_TRADE_INSTITUTION = "auto.trade.institution"
}
```

새 이벤트 클래스 추가:
```kotlin
/**
 * 자동매매 트리거 이벤트 (scheduler → trading)
 * 실제 매매 로직은 trading-service에서 처리
 */
data class AutoTradeSignalEvent(
    val investorType: String,  // "NPC" 또는 "INSTITUTION"
    val triggeredAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "AUTO_TRADE_SIGNAL"
}
```

---

### Step 2: scheduler-service - 스케줄러 추가

**새 파일: `backend/scheduler-service/.../infrastructure/adapter/in/scheduler/AutoTradingScheduler.kt`**

```kotlin
package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.common.event.AutoTradeSignalEvent
import com.stocksimulator.common.event.KafkaTopics
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AutoTradingScheduler(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * NPC 자동매매 트리거 - 5분마다
     * 실제 매매 로직은 trading-service에서 처리
     */
    @Scheduled(fixedRate = 300_000)  // 5분
    fun triggerNpcTrading() {
        log.info("NPC 자동매매 트리거 발행")
        val event = AutoTradeSignalEvent(investorType = "NPC")
        kafkaTemplate.send(KafkaTopics.AUTO_TRADE_NPC, event)
    }

    /**
     * 기관 자동매매 트리거 - 15분마다
     * 실제 매매 로직은 trading-service에서 처리
     */
    @Scheduled(fixedRate = 900_000)  // 15분
    fun triggerInstitutionTrading() {
        log.info("기관 자동매매 트리거 발행")
        val event = AutoTradeSignalEvent(investorType = "INSTITUTION")
        kafkaTemplate.send(KafkaTopics.AUTO_TRADE_INSTITUTION, event)
    }
}
```

> **참고**: 기존 `StockListingScheduler.kt`의 패턴을 그대로 따름.
> scheduler-service는 이미 `@EnableScheduling`이 있으므로 추가 설정 불필요.

---

### Step 3: trading-service - 투자자 잔고 계층 (Hexagonal Architecture)

`trading.investor_balances` 테이블은 V3 마이그레이션에서 이미 생성되어 있다. 이를 활용할 JPA 계층을 만든다.

#### 3a. Domain Model

**새 파일: `backend/trading-service/.../domain/model/InvestorBalanceModel.kt`**

```kotlin
package com.stocksimulator.tradingservice.domain.model

import java.time.Instant

data class InvestorBalanceModel(
    val id: Long? = null,
    val investorId: String,
    val investorType: String,   // "NPC", "INSTITUTION"
    val balance: Long,          // 현재 잔고 (원)
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
```

#### 3b. Port (Out)

**새 파일: `backend/trading-service/.../application/port/out/balance/InvestorBalancePersistencePort.kt`**

```kotlin
package com.stocksimulator.tradingservice.application.port.out.balance

import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel

interface InvestorBalancePersistencePort {
    fun findByInvestorIdAndType(investorId: String, investorType: String): InvestorBalanceModel?
    fun findAllByInvestorType(investorType: String): List<InvestorBalanceModel>
    fun save(balance: InvestorBalanceModel): InvestorBalanceModel
    fun updateBalance(investorId: String, investorType: String, newBalance: Long)
}
```

#### 3c. JPA Entity

**새 파일: `backend/trading-service/.../infrastructure/adapter/out/persistence/balance/entity/InvestorBalanceJpaEntity.kt`**

```kotlin
package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.balance.entity

import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "investor_balances",
    schema = "trading",
    indexes = [
        Index(name = "idx_investor_balances_investor_id", columnList = "investor_id")
    ]
)
class InvestorBalanceJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "investor_id", nullable = false, length = 36)
    val investorId: String,

    @Column(name = "investor_type", nullable = false, length = 20)
    val investorType: String,

    @Column(name = "balance", nullable = false)
    var balance: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): InvestorBalanceModel = InvestorBalanceModel(
        id = id,
        investorId = investorId,
        investorType = investorType,
        balance = balance,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: InvestorBalanceModel): InvestorBalanceJpaEntity =
            InvestorBalanceJpaEntity(
                id = domain.id,
                investorId = domain.investorId,
                investorType = domain.investorType,
                balance = domain.balance,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
    }
}
```

#### 3d. JPA Repository

**새 파일: `backend/trading-service/.../infrastructure/adapter/out/persistence/balance/repository/InvestorBalanceJpaRepository.kt`**

```kotlin
package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.balance.repository

import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.balance.entity.InvestorBalanceJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface InvestorBalanceJpaRepository : JpaRepository<InvestorBalanceJpaEntity, Long> {
    fun findByInvestorIdAndInvestorType(investorId: String, investorType: String): InvestorBalanceJpaEntity?
    fun findByInvestorType(investorType: String): List<InvestorBalanceJpaEntity>

    @Modifying
    @Query("UPDATE InvestorBalanceJpaEntity e SET e.balance = :newBalance, e.updatedAt = CURRENT_TIMESTAMP WHERE e.investorId = :investorId AND e.investorType = :investorType")
    fun updateBalance(investorId: String, investorType: String, newBalance: Long)
}
```

#### 3e. Persistence Adapter

**새 파일: `backend/trading-service/.../infrastructure/adapter/out/persistence/balance/adapter/InvestorBalancePersistenceAdapter.kt`**

```kotlin
package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.balance.adapter

import com.stocksimulator.tradingservice.application.port.out.balance.InvestorBalancePersistencePort
import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.balance.entity.InvestorBalanceJpaEntity
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.balance.repository.InvestorBalanceJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class InvestorBalancePersistenceAdapter(
    private val repository: InvestorBalanceJpaRepository
) : InvestorBalancePersistencePort {

    override fun findByInvestorIdAndType(investorId: String, investorType: String): InvestorBalanceModel? {
        return repository.findByInvestorIdAndInvestorType(investorId, investorType)?.toDomain()
    }

    override fun findAllByInvestorType(investorType: String): List<InvestorBalanceModel> {
        return repository.findByInvestorType(investorType).map { it.toDomain() }
    }

    override fun save(balance: InvestorBalanceModel): InvestorBalanceModel {
        return repository.save(InvestorBalanceJpaEntity.fromDomain(balance)).toDomain()
    }

    @Transactional
    override fun updateBalance(investorId: String, investorType: String, newBalance: Long) {
        repository.updateBalance(investorId, investorType, newBalance)
    }
}
```

---

### Step 4: trading-service - Portfolio 계층 확장

기존 `PortfolioPersistencePort`에 메서드 추가 필요.

#### 4a. Port 확장

**수정: `PortfolioPersistencePort.kt`** - 메서드 추가

```kotlin
interface PortfolioPersistencePort {
    fun save(portfolio: PortfolioModel): PortfolioModel
    fun saveAll(portfolios: List<PortfolioModel>): List<PortfolioModel>
    fun findByInvestorAndStock(investorId: String, investorType: String, stockId: String): PortfolioModel?

    // 추가
    fun findAllByInvestorId(investorId: String): List<PortfolioModel>
    fun findAllByInvestorType(investorType: String): List<PortfolioModel>
}
```

#### 4b. Repository 확장

**수정: `PortfolioJpaRepository.kt`** - 메서드 추가

```kotlin
interface PortfolioJpaRepository : JpaRepository<PortfolioJpaEntity, Long> {
    fun findByInvestorIdAndInvestorTypeAndStockId(
        investorId: String, investorType: String, stockId: String
    ): PortfolioJpaEntity?
    fun findByStockId(stockId: String): List<PortfolioJpaEntity>

    // 추가
    fun findByInvestorId(investorId: String): List<PortfolioJpaEntity>
    fun findByInvestorType(investorType: String): List<PortfolioJpaEntity>
}
```

#### 4c. Adapter 확장

**수정: `PortfolioPersistenceAdapter.kt`** - 메서드 구현 추가

```kotlin
// 기존 메서드들 아래에 추가

override fun findAllByInvestorId(investorId: String): List<PortfolioModel> {
    return portfolioJpaRepository.findByInvestorId(investorId).map { it.toDomain() }
}

override fun findAllByInvestorType(investorType: String): List<PortfolioModel> {
    return portfolioJpaRepository.findByInvestorType(investorType).map { it.toDomain() }
}
```

---

### Step 5: trading-service - SystemConstants 확장

**수정: `SystemConstants.kt`** - 투자 스타일 매핑 추가

```kotlin
object SystemConstants {
    const val SYSTEM_IPO_USER_ID = "SYSTEM_IPO"

    val SYSTEM_INSTITUTIONS = (1..5).map { "SYSTEM_INST_%02d".format(it) }
    val SYSTEM_NPCS = (1..10).map { "SYSTEM_NPC_%02d".format(it) }

    val NPC_SECTOR_PREFERENCES: Map<String, List<Sector>> = mapOf(
        // ... 기존 그대로 ...
    )

    // ===== 추가 =====

    // NPC별 투자 스타일
    val NPC_INVESTMENT_STYLES: Map<String, InvestmentStyle> = mapOf(
        "SYSTEM_NPC_01" to InvestmentStyle.AGGRESSIVE,
        "SYSTEM_NPC_02" to InvestmentStyle.STABLE,
        "SYSTEM_NPC_03" to InvestmentStyle.VALUE,
        "SYSTEM_NPC_04" to InvestmentStyle.RANDOM,
        "SYSTEM_NPC_05" to InvestmentStyle.AGGRESSIVE,
        "SYSTEM_NPC_06" to InvestmentStyle.STABLE,
        "SYSTEM_NPC_07" to InvestmentStyle.VALUE,
        "SYSTEM_NPC_08" to InvestmentStyle.RANDOM,
        "SYSTEM_NPC_09" to InvestmentStyle.AGGRESSIVE,
        "SYSTEM_NPC_10" to InvestmentStyle.STABLE
    )

    // 기관별 투자 스타일
    val INSTITUTION_INVESTMENT_STYLES: Map<String, InvestmentStyle> = mapOf(
        "SYSTEM_INST_01" to InvestmentStyle.STABLE,
        "SYSTEM_INST_02" to InvestmentStyle.VALUE,
        "SYSTEM_INST_03" to InvestmentStyle.AGGRESSIVE,
        "SYSTEM_INST_04" to InvestmentStyle.STABLE,
        "SYSTEM_INST_05" to InvestmentStyle.VALUE
    )

    // 투자 스타일별 매매 파라미터
    data class TradingParams(
        val tradeProbability: Double,      // 매매 확률
        val buyRatioMin: Double,           // 잔고 대비 최소 매수 비율
        val buyRatioMax: Double,           // 잔고 대비 최대 매수 비율
        val sellRatioMin: Double,          // 보유량 대비 최소 매도 비율
        val sellRatioMax: Double,          // 보유량 대비 최대 매도 비율
        val marketOrderProbability: Double // 시장가 주문 확률
    )

    val STYLE_TRADING_PARAMS: Map<InvestmentStyle, TradingParams> = mapOf(
        InvestmentStyle.AGGRESSIVE to TradingParams(
            tradeProbability = 0.70,
            buyRatioMin = 0.10, buyRatioMax = 0.20,
            sellRatioMin = 0.10, sellRatioMax = 0.30,
            marketOrderProbability = 0.40
        ),
        InvestmentStyle.STABLE to TradingParams(
            tradeProbability = 0.40,
            buyRatioMin = 0.05, buyRatioMax = 0.10,
            sellRatioMin = 0.05, sellRatioMax = 0.15,
            marketOrderProbability = 0.20
        ),
        InvestmentStyle.VALUE to TradingParams(
            tradeProbability = 0.50,
            buyRatioMin = 0.08, buyRatioMax = 0.15,
            sellRatioMin = 0.05, sellRatioMax = 0.20,
            marketOrderProbability = 0.10
        ),
        InvestmentStyle.RANDOM to TradingParams(
            tradeProbability = 0.60,
            buyRatioMin = 0.05, buyRatioMax = 0.20,
            sellRatioMin = 0.05, sellRatioMax = 0.30,
            marketOrderProbability = 0.50
        )
    )
}
```

> `InvestmentStyle` import: `com.stocksimulator.common.dto.InvestmentStyle`

---

### Step 6: trading-service - OrderBookRegistry 확장

**수정: `OrderBookRegistry.kt`** - 활성 종목 목록 메서드 추가

```kotlin
// 기존 코드에 메서드 1개만 추가

/**
 * 호가창이 활성화된 (등록된) 종목 ID 목록 반환
 */
fun getActiveStockIds(): Set<String> {
    return orderBooks.keys.toSet()
}
```

> `orderBooks`는 이미 `ConcurrentHashMap<String, OrderBookDomainService>`로 선언되어 있음

---

### Step 7: trading-service - AutoTradingHandler (핵심 매매 로직)

**새 파일: `backend/trading-service/.../application/handler/autotrade/AutoTradingHandler.kt`**

```kotlin
package com.stocksimulator.tradingservice.application.handler.autotrade

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.common.util.PriceUtil
import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.handler.order.OrderBookRegistry
import com.stocksimulator.tradingservice.application.port.`in`.order.PlaceOrderUseCase
import com.stocksimulator.tradingservice.application.port.out.balance.InvestorBalancePersistencePort
import com.stocksimulator.tradingservice.application.port.out.portfolio.PortfolioPersistencePort
import com.stocksimulator.tradingservice.domain.SystemConstants
import com.stocksimulator.tradingservice.domain.SystemConstants.STYLE_TRADING_PARAMS
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshotVo
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class AutoTradingHandler(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val orderBookRegistry: OrderBookRegistry,
    private val portfolioPersistencePort: PortfolioPersistencePort,
    private val balancePersistencePort: InvestorBalancePersistencePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * NPC 자동매매 실행 - 각 NPC별로 코루틴으로 개별 실행
     */
    suspend fun executeNpcTrading() = coroutineScope {
        val activeStockIds = orderBookRegistry.getActiveStockIds()
        if (activeStockIds.isEmpty()) {
            log.debug("자동매매 스킵 - 활성 종목 없음")
            return@coroutineScope
        }

        val jobs = SystemConstants.SYSTEM_NPCS.map { npcId ->
            launch(Dispatchers.IO) {
                try {
                    executeInvestorTrading(
                        investorId = npcId,
                        investorType = "NPC",
                        style = SystemConstants.NPC_INVESTMENT_STYLES[npcId] ?: InvestmentStyle.RANDOM,
                        activeStockIds = activeStockIds,
                        sectorPreferences = SystemConstants.NPC_SECTOR_PREFERENCES[npcId]
                    )
                } catch (e: Exception) {
                    log.warn("NPC 자동매매 실패: npcId={}, error={}", npcId, e.message)
                }
            }
        }

        jobs.joinAll()
        log.info("NPC 자동매매 완료: {}명 실행", SystemConstants.SYSTEM_NPCS.size)
    }

    /**
     * 기관 자동매매 실행 - 각 기관별로 코루틴으로 개별 실행
     */
    suspend fun executeInstitutionTrading() = coroutineScope {
        val activeStockIds = orderBookRegistry.getActiveStockIds()
        if (activeStockIds.isEmpty()) {
            log.debug("자동매매 스킵 - 활성 종목 없음")
            return@coroutineScope
        }

        val jobs = SystemConstants.SYSTEM_INSTITUTIONS.map { instId ->
            launch(Dispatchers.IO) {
                try {
                    executeInvestorTrading(
                        investorId = instId,
                        investorType = "INSTITUTION",
                        style = SystemConstants.INSTITUTION_INVESTMENT_STYLES[instId] ?: InvestmentStyle.STABLE,
                        activeStockIds = activeStockIds,
                        sectorPreferences = null  // 기관은 섹터 제한 없음
                    )
                } catch (e: Exception) {
                    log.warn("기관 자동매매 실패: instId={}, error={}", instId, e.message)
                }
            }
        }

        jobs.joinAll()
        log.info("기관 자동매매 완료: {}개 기관 실행", SystemConstants.SYSTEM_INSTITUTIONS.size)
    }

    /**
     * 개별 투자자의 매매 실행
     */
    private fun executeInvestorTrading(
        investorId: String,
        investorType: String,
        style: InvestmentStyle,
        activeStockIds: Set<String>,
        sectorPreferences: List<com.stocksimulator.common.dto.Sector>?
    ) {
        val params = STYLE_TRADING_PARAMS[style] ?: return

        // 매매 확률 체크
        if (Random.nextDouble() > params.tradeProbability) {
            log.debug("자동매매 스킵 (확률 미달): investorId={}", investorId)
            return
        }

        // 잔고 조회
        val balanceModel = balancePersistencePort.findByInvestorIdAndType(investorId, investorType)
        val balance = balanceModel?.balance ?: 0L

        // 포트폴리오 조회
        val portfolios = portfolioPersistencePort.findAllByInvestorId(investorId)

        // 매수 or 매도 결정 (50:50 기본, 잔고/포트폴리오 상황에 따라 조정)
        val buyProbability = when {
            balance <= 0 && portfolios.isEmpty() -> 0.0  // 잔고도 주식도 없음
            balance <= 0 -> 0.0                          // 잔고 없으면 매도만
            portfolios.isEmpty() -> 1.0                  // 주식 없으면 매수만
            else -> 0.5
        }

        if (Random.nextDouble() < buyProbability) {
            executeBuy(investorId, investorType, balance, params, activeStockIds, sectorPreferences)
        } else if (portfolios.isNotEmpty()) {
            executeSell(investorId, investorType, portfolios, params, activeStockIds)
        }
    }

    /**
     * 매수 실행
     */
    private fun executeBuy(
        investorId: String,
        investorType: String,
        balance: Long,
        params: SystemConstants.TradingParams,
        activeStockIds: Set<String>,
        sectorPreferences: List<com.stocksimulator.common.dto.Sector>?
    ) {
        if (balance <= 0) return

        // 종목 선택 (NPC: 선호 섹터 기반, 기관: 랜덤)
        // TODO: sectorPreferences를 활용해 선호 종목 필터링하려면
        //       stock-service에서 종목-섹터 매핑 정보가 필요함.
        //       우선은 활성 종목 중 호가창이 있는 것에서 랜덤 선택
        val targetStockId = activeStockIds.randomOrNull() ?: return

        // 호가창 조회
        val snapshot = orderBookRegistry.getSnapshot(targetStockId)
        val bestAsk = snapshot.bestAsk ?: return  // 매도호가 없으면 매수 불가

        // 주문 금액 결정 (잔고의 buyRatioMin ~ buyRatioMax)
        val ratio = Random.nextDouble(params.buyRatioMin, params.buyRatioMax)
        val orderAmount = (balance * ratio).toLong()
        if (orderAmount <= 0) return

        // 수량 계산
        val quantity = orderAmount / bestAsk
        if (quantity <= 0) return

        // 주문 유형 결정 (시장가 vs 지정가)
        val isMarketOrder = Random.nextDouble() < params.marketOrderProbability

        val (orderKind, price) = if (isMarketOrder) {
            OrderKind.MARKET to null
        } else {
            // 지정가: bestAsk 기준 -2틱 ~ +2틱
            val tickSize = PriceUtil.getTickSize(bestAsk)
            val tickOffset = Random.nextInt(-2, 3)  // -2, -1, 0, 1, 2
            val targetPrice = PriceUtil.adjustPriceUp(bestAsk + tickSize * tickOffset)
            OrderKind.LIMIT to maxOf(targetPrice, tickSize) // 최소 1틱 보장
        }

        try {
            val command = PlaceOrderCommand(
                userId = investorId,
                stockId = targetStockId,
                orderType = OrderType.BUY,
                orderKind = orderKind,
                price = price,
                quantity = quantity
            )
            placeOrderUseCase.placeOrder(command)

            log.info(
                "자동매수 실행: investorId={}, stockId={}, {}={}, 수량={}",
                investorId, targetStockId,
                if (isMarketOrder) "시장가" else "지정가", price ?: "시장가",
                quantity
            )
        } catch (e: Exception) {
            log.warn("자동매수 실패: investorId={}, stockId={}, error={}", investorId, targetStockId, e.message)
        }
    }

    /**
     * 매도 실행
     */
    private fun executeSell(
        investorId: String,
        investorType: String,
        portfolios: List<PortfolioModel>,
        params: SystemConstants.TradingParams,
        activeStockIds: Set<String>
    ) {
        // 활성 종목 중 보유 종목 필터
        val sellablePortfolios = portfolios.filter {
            it.stockId in activeStockIds && it.quantity > 0
        }
        if (sellablePortfolios.isEmpty()) return

        val targetPortfolio = sellablePortfolios.random()

        // 호가창 조회
        val snapshot = orderBookRegistry.getSnapshot(targetPortfolio.stockId)
        val bestBid = snapshot.bestBid  // 매수호가 없어도 시장가는 가능

        // 매도 수량 결정 (보유량의 sellRatioMin ~ sellRatioMax)
        val ratio = Random.nextDouble(params.sellRatioMin, params.sellRatioMax)
        val quantity = maxOf((targetPortfolio.quantity * ratio).toLong(), 1L)

        // 수익률 기반 매도 확률 조정
        if (bestBid != null) {
            val profitRate = (bestBid - targetPortfolio.averagePrice).toDouble() / targetPortfolio.averagePrice
            // 수익률 높으면 → 익절 확률 ↑, 손실이면 → 손절 확률 (스타일에 따라)
            val sellDecision = when {
                profitRate > 0.20 -> true   // 20% 이상 수익 → 익절
                profitRate < -0.15 -> Random.nextDouble() < 0.3  // 15% 이상 손실 → 30% 확률 손절
                else -> true  // 기본적으로 매도 진행
            }
            if (!sellDecision) return
        }

        // 주문 유형 결정
        val isMarketOrder = Random.nextDouble() < params.marketOrderProbability

        val (orderKind, price) = if (isMarketOrder || bestBid == null) {
            OrderKind.MARKET to null
        } else {
            val tickSize = PriceUtil.getTickSize(bestBid)
            val tickOffset = Random.nextInt(-2, 3)
            val targetPrice = PriceUtil.adjustPriceDown(bestBid + tickSize * tickOffset)
            OrderKind.LIMIT to maxOf(targetPrice, tickSize)
        }

        try {
            val command = PlaceOrderCommand(
                userId = investorId,
                stockId = targetPortfolio.stockId,
                orderType = OrderType.SELL,
                orderKind = orderKind,
                price = price,
                quantity = quantity
            )
            placeOrderUseCase.placeOrder(command)

            log.info(
                "자동매도 실행: investorId={}, stockId={}, {}={}, 수량={}",
                investorId, targetPortfolio.stockId,
                if (isMarketOrder) "시장가" else "지정가", price ?: "시장가",
                quantity
            )
        } catch (e: Exception) {
            log.warn("자동매도 실패: investorId={}, stockId={}, error={}", investorId, targetPortfolio.stockId, e.message)
        }
    }
}
```

---

### Step 8: trading-service - Kafka Consumer 확장

**수정: `TradingEventConsumer.kt`** - 자동매매 이벤트 리스너 추가

```kotlin
// 기존 import에 추가
import com.stocksimulator.common.event.AutoTradeSignalEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.tradingservice.application.handler.autotrade.AutoTradingHandler
import kotlinx.coroutines.runBlocking

@Component
class TradingEventConsumer(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val seedIpoOrderBookUseCase: SeedIpoOrderBookUseCase,
    private val autoTradingHandler: AutoTradingHandler   // 추가
) {
    // ... 기존 메서드 그대로 유지 ...

    @KafkaListener(topics = [KafkaTopics.AUTO_TRADE_NPC], groupId = "trading-service")
    fun handleNpcAutoTrade(event: AutoTradeSignalEvent, ack: Acknowledgment) {
        try {
            log.info("NPC 자동매매 시그널 수신")
            runBlocking {
                autoTradingHandler.executeNpcTrading()
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("NPC 자동매매 처리 실패", e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.AUTO_TRADE_INSTITUTION], groupId = "trading-service")
    fun handleInstitutionAutoTrade(event: AutoTradeSignalEvent, ack: Acknowledgment) {
        try {
            log.info("기관 자동매매 시그널 수신")
            runBlocking {
                autoTradingHandler.executeInstitutionTrading()
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("기관 자동매매 처리 실패", e)
            ack.acknowledge()
        }
    }
}
```

---

### Step 9: 초기 잔고 시딩 (DB)

NPC/기관이 매수하려면 잔고가 필요하다. `investor_balances` 테이블에 초기 잔고를 넣어야 한다.

**Flyway 마이그레이션 또는 수동 SQL:**

**파일: `V5__seed_investor_balances.sql`** (또는 수동 실행)

```sql
-- NPC 초기 잔고 (20만 ~ 1억)
INSERT INTO trading.investor_balances (investor_id, investor_type, balance, created_at, updated_at)
VALUES
    ('SYSTEM_NPC_01', 'NPC', 50000000, NOW(), NOW()),   -- 5천만
    ('SYSTEM_NPC_02', 'NPC', 30000000, NOW(), NOW()),   -- 3천만
    ('SYSTEM_NPC_03', 'NPC', 80000000, NOW(), NOW()),   -- 8천만
    ('SYSTEM_NPC_04', 'NPC', 20000000, NOW(), NOW()),   -- 2천만
    ('SYSTEM_NPC_05', 'NPC', 100000000, NOW(), NOW()),  -- 1억
    ('SYSTEM_NPC_06', 'NPC', 15000000, NOW(), NOW()),   -- 1500만
    ('SYSTEM_NPC_07', 'NPC', 60000000, NOW(), NOW()),   -- 6천만
    ('SYSTEM_NPC_08', 'NPC', 40000000, NOW(), NOW()),   -- 4천만
    ('SYSTEM_NPC_09', 'NPC', 25000000, NOW(), NOW()),   -- 2500만
    ('SYSTEM_NPC_10', 'NPC', 10000000, NOW(), NOW())    -- 1천만
ON CONFLICT (investor_id, investor_type) DO NOTHING;

-- 기관 초기 잔고 (10억 ~ 1조)
INSERT INTO trading.investor_balances (investor_id, investor_type, balance, created_at, updated_at)
VALUES
    ('SYSTEM_INST_01', 'INSTITUTION', 50000000000, NOW(), NOW()),   -- 500억
    ('SYSTEM_INST_02', 'INSTITUTION', 100000000000, NOW(), NOW()),  -- 1000억
    ('SYSTEM_INST_03', 'INSTITUTION', 30000000000, NOW(), NOW()),   -- 300억
    ('SYSTEM_INST_04', 'INSTITUTION', 200000000000, NOW(), NOW()),  -- 2000억
    ('SYSTEM_INST_05', 'INSTITUTION', 80000000000, NOW(), NOW())    -- 800억
ON CONFLICT (investor_id, investor_type) DO NOTHING;
```

---

### Step 10: Kafka 설정 - trusted packages 확인

`trading-service`의 `application.yml`에 이미 trusted packages가 설정되어 있다:
```yaml
spring.json.trusted.packages: "com.stocksimulator.common.event,com.stocksimulator.common.dto"
```

`AutoTradeSignalEvent`는 `com.stocksimulator.common.event` 패키지이므로 추가 설정 불필요.

---

## 스케줄러 시간 조정 제안

현재 상황: NPC가 많고 주식 수가 적음.

### 변경 전 → 변경 후

| 스케줄러 | 현재 | 제안 | 이유 |
|---------|------|------|------|
| IPO (주식 상장) | 30분, 40% | **15분, 50%** | 주식 수 빠르게 늘리기 |
| NPC 자동매매 | - | **5분** | 유동성 공급 |
| 기관 자동매매 | - | **15분** | 대규모 주문 간격 |
| 상장폐지 | 1시간, 10% | **2시간, 5%** | 주식 수 보존 |

**수정 파일: `StockListingScheduler.kt`**
```kotlin
@Scheduled(fixedRate = 900_000)   // 30분 → 15분
fun checkForIPO() {
    if (roll < 0.5) {  // 0.4 → 0.5
        // ...
    }
}

@Scheduled(fixedRate = 7_200_000)  // 1시간 → 2시간
fun checkForDelisting() {
    if (roll < 0.05) {  // 0.1 → 0.05
        // ...
    }
}
```

---

## 파일 목록 요약

### 새로 만들 파일

| # | 파일 | 서비스 |
|---|------|--------|
| 1 | `domain/model/InvestorBalanceModel.kt` | trading-service |
| 2 | `application/port/out/balance/InvestorBalancePersistencePort.kt` | trading-service |
| 3 | `infrastructure/.../persistence/balance/entity/InvestorBalanceJpaEntity.kt` | trading-service |
| 4 | `infrastructure/.../persistence/balance/repository/InvestorBalanceJpaRepository.kt` | trading-service |
| 5 | `infrastructure/.../persistence/balance/adapter/InvestorBalancePersistenceAdapter.kt` | trading-service |
| 6 | `application/handler/autotrade/AutoTradingHandler.kt` | trading-service |
| 7 | `infrastructure/.../in/scheduler/AutoTradingScheduler.kt` | scheduler-service |
| 8 | `db/migration/V5__seed_investor_balances.sql` | trading-service |

### 수정할 파일

| # | 파일 | 변경 내용 |
|---|------|----------|
| 1 | `common/.../event/DomainEvent.kt` | KafkaTopics + AutoTradeSignalEvent 추가 |
| 2 | `trading-service/.../SystemConstants.kt` | 투자 스타일 매핑 + TradingParams 추가 |
| 3 | `trading-service/.../PortfolioPersistencePort.kt` | findAllByInvestorId/Type 추가 |
| 4 | `trading-service/.../PortfolioJpaRepository.kt` | findByInvestorId/Type 추가 |
| 5 | `trading-service/.../PortfolioPersistenceAdapter.kt` | 새 메서드 구현 추가 |
| 6 | `trading-service/.../OrderBookRegistry.kt` | getActiveStockIds() 추가 |
| 7 | `trading-service/.../TradingEventConsumer.kt` | 자동매매 리스너 2개 추가 |
| 8 | `scheduler-service/.../StockListingScheduler.kt` | IPO/상폐 시간 조정 |

### 수정 불필요 (그대로 재사용)

| 파일 | 용도 |
|------|------|
| `PlaceOrderCommand.kt` | 주문 커맨드 |
| `OrderCommandHandler.kt` | 주문 실행 로직 |
| `OrderBookDomainService.kt` | 호가창 매칭 엔진 |
| `PriceUtil.kt` | 호가 단위 계산 |
| `OrderModel.kt` | 주문 도메인 모델 |
| `PortfolioModel.kt` | 포트폴리오 도메인 모델 |

---

## 검증 방법

```bash
# 1. 빌드 확인
./gradlew :backend:common:build -x test
./gradlew :backend:scheduler-service:build -x test
./gradlew :backend:trading-service:build -x test

# 2. Docker 재빌드
docker-compose --profile all up -d --build scheduler-service trading-service

# 3. 스케줄러 로그 확인
docker logs stockSimulator-scheduler-service 2>&1 | grep "자동매매"

# 4. 매매 실행 로그 확인
docker logs stockSimulator-trading-service 2>&1 | grep "자동매"

# 5. DB 확인 - NPC/기관 주문
docker exec stockSimulator-postgres psql -U stocksim -d stocksimulator -c \
  "SELECT investor_type, order_type, COUNT(*) FROM trading.orders WHERE investor_type IN ('NPC','INSTITUTION') GROUP BY investor_type, order_type;"

# 6. 잔고 확인
docker exec stockSimulator-postgres psql -U stocksim -d stocksimulator -c \
  "SELECT * FROM trading.investor_balances ORDER BY investor_type, investor_id;"
```

---

## 주의사항

1. **잔고 차감 로직**: 현재 `PlaceOrderUseCase`에 잔고 차감 로직이 없음. 매수 시 잔고가 줄지 않고 무한 매수 가능한 상태. 향후 `OrderCommandHandler.placeOrder()`에서 `investor_balances` 잔고 확인/차감 로직 추가 필요.

2. **포트폴리오 업데이트**: 체결 시 포트폴리오 수량을 업데이트하는 로직도 현재 없음. 매도 체결 시 `portfolios.quantity` 차감, 매수 체결 시 추가/생성 로직 필요.

3. **코루틴 의존성**: `kotlinx-coroutines-core`가 trading-service의 `build.gradle.kts`에 있는지 확인 필요. 없으면 추가:
   ```kotlin
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
   ```

4. **Kafka 토픽 자동 생성**: Spring Kafka는 기본적으로 리스너가 등록된 토픽을 auto-create함. 수동 생성이 필요하면 Kafka UI에서 `auto.trade.npc`, `auto.trade.institution` 토픽 생성.
