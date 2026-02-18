package com.stocksimulator.eventservice.domain

data class RandomStockInfo(
    val stockId: String,
    val stockName: String,
    val currentPrice: Long
)
