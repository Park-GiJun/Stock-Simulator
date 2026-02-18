package com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto

data class RandomStockResponse(
    val stockId: String,
    val stockName: String,
    val currentPrice: Long
)
