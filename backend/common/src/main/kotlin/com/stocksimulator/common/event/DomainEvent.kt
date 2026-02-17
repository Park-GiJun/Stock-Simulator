package com.stocksimulator.common.event

import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.common.dto.Sentiment
import java.time.Instant
import java.util.UUID

/**
 * 도메인 이벤트 베이스 클래스
 */
abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now()
) {
    abstract val eventType: String
}

/**
 * Kafka 토픽 정의
 */
object KafkaTopics {
    // Trading 관련
    const val ORDER_CREATED = "order.created"
    const val ORDER_MATCHED = "order.matched"
    const val ORDER_CANCELLED = "order.cancelled"
    const val TRADE_EXECUTED = "trade.executed"

    // Stock 관련
    const val PRICE_UPDATED = "price.updated"
    const val ORDERBOOK_UPDATED = "orderbook.updated"

    // Event 관련
    const val EVENT_OCCURRED = "event.occurred"

    // News 관련
    const val NEWS_PUBLISHED = "news.published"

    // Ranking 관련
    const val RANKING_UPDATED = "ranking.updated"

    // Scheduler 관련
    const val SCHEDULE_TRADE = "schedule.trade"
    const val INCOME_DISTRIBUTED = "income.distributed"

    // User 관련
    const val USER_CREATED = "user.created"
    const val USER_UPDATED = "user.updated"

    // Stock Listing 관련
    const val STOCK_LISTED = "stock.listed"
    const val STOCK_DELISTED = "stock.delisted"

    // Investor 관련
    const val INVESTOR_CREATED = "investor.created"
}

// ===== Trading Events =====

data class OrderCreatedEvent(
    val orderId: String,
    val userId: String,
    val stockId: String,
    val orderType: OrderType,
    val quantity: Long,
    val price: Long?, // null = 시장가
    val createdAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "ORDER_CREATED"
}

data class OrderMatchedEvent(
    val tradeId: String,
    val buyOrderId: String,
    val sellOrderId: String,
    val buyUserId: String,
    val sellUserId: String,
    val stockId: String,
    val price: Long,
    val quantity: Long,
    val matchedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "ORDER_MATCHED"
}

data class OrderCancelledEvent(
    val orderId: String,
    val userId: String,
    val stockId: String,
    val reason: String,
    val cancelledAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "ORDER_CANCELLED"
}

// ===== Stock Events =====

data class PriceUpdatedEvent(
    val stockId: String,
    val previousPrice: Long,
    val currentPrice: Long,
    val changeRate: Double,
    val volume: Long,
    val updatedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "PRICE_UPDATED"
}

data class OrderBookUpdatedEvent(
    val stockId: String,
    val bidOrders: List<OrderBookEntry>,
    val askOrders: List<OrderBookEntry>,
    val updatedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "ORDERBOOK_UPDATED"
}

data class OrderBookEntry(
    val price: Long,
    val quantity: Long,
    val orderCount: Int
)

// ===== Event Service Events =====

data class EventOccurredEvent(
    val gameEventId: String,
    val level: String,       // SOCIETY, INDUSTRY, COMPANY
    val targetSector: String?,
    val targetStockId: String?,
    val sentiment: Sentiment,
    val intensity: Double,
    val duration: Long,      // 게임 시간 기준 (분)
    val headline: String,
    val content: String,
    val occurredAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "EVENT_OCCURRED"
}

// ===== News Events =====

data class NewsPublishedEvent(
    val newsId: String,
    val gameEventId: String?,
    val headline: String,
    val summary: String,
    val sector: String?,
    val stockId: String?,
    val publishedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "NEWS_PUBLISHED"
}

// ===== Ranking Events =====

data class RankingUpdatedEvent(
    val userId: String,
    val previousRank: Int,
    val currentRank: Int,
    val rankingType: String, // RETURN_RATE, TOTAL_ASSET, TRADE_VOLUME
    val score: Double,
    val updatedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "RANKING_UPDATED"
}

// ===== Scheduler Events =====

data class ScheduleTradeEvent(
    val investorId: String,
    val investorType: String, // NPC, INSTITUTION
    val stockId: String,
    val orderType: OrderType,
    val quantity: Long,
    val reason: String,
    val scheduledAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "SCHEDULE_TRADE"
}

data class IncomeDistributedEvent(
    val investorId: String,
    val investorType: String,
    val amount: Long,
    val distributedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "INCOME_DISTRIBUTED"
}

// ===== User Events =====

data class UserCreatedEvent(
    val userId: String,
    val email: String,
    val nickname: String,
    val initialCapital: Long,
    val createdAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "USER_CREATED"
}

// ===== Stock Listing Events =====

data class StockListedEvent(
    val stockId: String,
    val stockName: String,
    val sector: String,
    val basePrice: Long,
    val totalShares: Long,
    val marketCapGrade: String,
    val volatility: Long = 0L,
    val per: Double = 0.0,
    val dividendRate: Double = 0.0,
    val growthRate: Double = 0.0,
    val eventSensitivity: Double = 0.0,
    val listedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "STOCK_LISTED"
}

data class StockDelistedEvent(
    val stockId: String,
    val stockName: String,
    val reason: String,
    val finalPrice: Long,
    val delistedAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "STOCK_DELISTED"
}

// ===== Investor Events =====

data class InvestorCreatedEvent(
    val investorId: String,
    val investorType: String,  // INDIVIDUAL, INSTITUTION
    val investmentStyle: String,
    val initialCapital: Long,
    val parameters: Map<String, Any>,
    val createdAt: Instant = Instant.now()
) : DomainEvent() {
    override val eventType: String = "INVESTOR_CREATED"
}
