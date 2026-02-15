package com.stocksimulator.stockservice.infrastructure.adapter.`in`.web.dto

data class StockExistsResponse(
    val stockIdExists: Boolean,
    val stockNameExists: Boolean
)
