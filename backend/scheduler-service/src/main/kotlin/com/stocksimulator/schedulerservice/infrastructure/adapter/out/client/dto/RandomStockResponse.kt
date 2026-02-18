package com.stocksimulator.schedulerservice.infrastructure.adapter.out.client.dto

data class RandomStockResponse(
    val stockId: String,
    val stockName: String,
    val currentPrice: Long
)