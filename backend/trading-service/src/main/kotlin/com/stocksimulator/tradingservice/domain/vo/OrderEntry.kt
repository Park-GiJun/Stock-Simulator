package com.stocksimulator.tradingservice.domain.vo

import java.time.Instant

data class OrderEntry(
    val orderId: String,
    val userId: String,
    val price: Long,
    var remainingQuantity: Long,
    val timestamp: Instant = Instant.now()
)
