package com.stocksimulator.tradingservice.domain.vo

import java.time.Instant

data class OrderBookSnapshotVo(
    val stockId: String,
    val bids: List<PriceLevelVo>,
    val asks: List<PriceLevelVo>,
    val bestBid: Long?,
    val bestAsk: Long?,
    val spread: Long?,
    val timestamp: Instant = Instant.now()
)
