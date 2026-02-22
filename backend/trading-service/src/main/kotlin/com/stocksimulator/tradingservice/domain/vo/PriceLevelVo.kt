package com.stocksimulator.tradingservice.domain.vo

data class PriceLevelVo(
    val price: Long,
    val quantity: Long,
    val orderCount: Int
)
