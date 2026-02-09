package com.stocksimulator.stockservice.application.dto.result.stock

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.stockservice.domain.StockModel

data class StockDetailResult(
    val stockId: String,
    val stockName: String,
    val sector: Sector,
    val basePrice: Long,
    val currentPrice: Long,
    val previousClose: Long,
    val change: Long,
    val changePercent: Double,
    val totalShares: Long,
    val marketCapGrade: MarketCapGrade,
    val volatility: Long,
    val per: Double,
    val dividendRate: Double,
    val growthRate: Double,
    val eventSensitivity: Double,
    val volume: Long,
    val high: Long,
    val low: Long,
    val open: Long,
    val status: StockStatus
) {
    companion object {
        fun from(stock: StockModel): StockDetailResult = StockDetailResult(
            stockId = stock.stockId,
            stockName = stock.stockName,
            sector = stock.sector,
            basePrice = stock.basePrice,
            currentPrice = stock.currentPrice,
            previousClose = stock.previousClose,
            change = stock.change,
            changePercent = stock.changePercent,
            totalShares = stock.totalShares,
            marketCapGrade = stock.marketCapGrade,
            volatility = stock.volatility,
            per = stock.per,
            dividendRate = stock.dividendRate,
            growthRate = stock.growthRate,
            eventSensitivity = stock.eventSensitivity,
            volume = stock.volume,
            high = stock.high,
            low = stock.low,
            open = stock.open,
            status = stock.status
        )
    }
}
