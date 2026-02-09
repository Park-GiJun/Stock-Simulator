package com.stocksimulator.stockservice.application.dto.result.stock

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.stockservice.domain.StockModel

data class StockListItemResult(
    val stockId: String,
    val stockName: String,
    val sector: Sector,
    val currentPrice: Long,
    val change: Long,
    val changePercent: Double,
    val volume: Long,
    val marketCapGrade: MarketCapGrade
) {
    companion object {
        fun from(stock: StockModel): StockListItemResult = StockListItemResult(
            stockId = stock.stockId,
            stockName = stock.stockName,
            sector = stock.sector,
            currentPrice = stock.currentPrice,
            change = stock.change,
            changePercent = stock.changePercent,
            volume = stock.volume,
            marketCapGrade = stock.marketCapGrade
        )
    }
}
