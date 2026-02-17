package com.stocksimulator.tradingservice.application.dto.query.order

data class OrderBookQuery(
    val stockId: String,
    val depth: Int = 10
)
