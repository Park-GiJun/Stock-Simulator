package com.stocksimulator.schedulerservice.domain

data class RandomStockInfo(
    val stockId: String,
    val stockName: String,
    val currentPrice: Long
)