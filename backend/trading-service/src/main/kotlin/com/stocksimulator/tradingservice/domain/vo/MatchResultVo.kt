package com.stocksimulator.tradingservice.domain.vo

import java.time.Instant
import java.util.UUID

data class MatchResultVo(
    val tradeId: String = UUID.randomUUID().toString(),
    val buyOrderId: String,
    val sellOrderId: String,
    val buyUserId: String,
    val sellUserId: String,
    val stockId: String,
    val price: Long,
    val quantity: Long,
    val matchedAt: Instant = Instant.now()
)
