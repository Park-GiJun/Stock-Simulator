package com.stocksimulator.stockservice.adapter.`in`.web.dto

data class StockExistsResponse(
    val stockIdExists: Boolean,
    val stockNameExists: Boolean
)
