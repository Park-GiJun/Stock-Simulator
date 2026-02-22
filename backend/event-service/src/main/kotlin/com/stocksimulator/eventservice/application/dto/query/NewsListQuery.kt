package com.stocksimulator.eventservice.application.dto.query

data class NewsListQuery(
    val page: Int = 0,
    val size: Int = 20,
    val level: String? = null,
    val sentiment: String? = null,
    val sector: String? = null,
    val stockId: String? = null
)
