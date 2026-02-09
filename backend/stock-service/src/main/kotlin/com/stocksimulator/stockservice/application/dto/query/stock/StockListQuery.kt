package com.stocksimulator.stockservice.application.dto.query.stock

data class StockListQuery(
    val page: Int = 0,
    val size: Int = 20,
    val sector: String? = null,
    val marketCap: String? = null,
    val sortBy: String = "stockName",
    val sortOrder: String = "asc",
    val search: String? = null
)
