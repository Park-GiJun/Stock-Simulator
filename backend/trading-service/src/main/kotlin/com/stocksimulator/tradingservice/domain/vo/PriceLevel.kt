package com.stocksimulator.tradingservice.domain.vo

data class PriceLevel(
    val price: Long,
    val quantity: Long,
    val orderCount: Int
)
