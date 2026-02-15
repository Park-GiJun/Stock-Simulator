package com.stocksimulator.schedulerservice.application.port.out

interface StockQueryPort {
    fun getRandomListedStock(): RandomStockInfo?
}

data class RandomStockInfo(
    val stockId: String,
    val stockName: String,
    val currentPrice: Long
)
