package com.stocksimulator.schedulerservice.infrastructure.adapter.out.client.dto

data class StockExistsResponse(
    val stockIdExists: Boolean = false,
    val stockNameExists: Boolean = false
)