package com.stocksimulator.common.event

import java.time.Instant
import java.util.UUID

abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val eventType: String = ""
)

// Trading Events
object KafkaTopics {
    const val ORDER_CREATED = "order.created"
    const val ORDER_MATCHED = "order.matched"
    const val ORDER_CANCELLED = "order.cancelled"
    const val PRICE_UPDATED = "price.updated"
    const val TRADE_EXECUTED = "trade.executed"
    const val EVENT_OCCURRED = "event.occurred"
    const val NEWS_PUBLISHED = "news.published"
    const val RANKING_UPDATED = "ranking.updated"
    const val SCHEDULE_TRADE = "schedule.trade"
}
