package com.stocksimulator.tradingservice.domain.vo

import java.time.Instant

data class OrderBookSnapshot(
    val stockId: String,
    val bids: List<PriceLevel>,
    val asks: List<PriceLevel>,
    val bestBid: Long?,
    val bestAsk: Long?,
    val spread: Long?,
    val timestamp: Instant = Instant.now()
)
