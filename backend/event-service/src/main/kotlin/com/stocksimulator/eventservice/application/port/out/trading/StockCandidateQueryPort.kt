package com.stocksimulator.eventservice.application.port.out.trading

interface StockCandidateQueryPort {
    fun getStocksBySector(sector: String, maxCount: Int): List<StockCandidateDto>
}

data class StockCandidateDto(
    val stockId: String,
    val stockName: String,
    val sector: String,
    val currentPrice: Long,
    val changePercent: Double
)
