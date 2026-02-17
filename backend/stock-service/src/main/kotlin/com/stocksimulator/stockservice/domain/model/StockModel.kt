package com.stocksimulator.stockservice.domain.model

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import java.time.Instant

data class StockModel(
    val stockId: String,
    val stockName: String,
    val sector: Sector,
    val basePrice: Long,
    val currentPrice: Long,
    val previousClose: Long,
    val totalShares: Long,
    val marketCapGrade: MarketCapGrade,
    val volatility: Long,
    val per: Double,
    val dividendRate: Double,
    val growthRate: Double,
    val eventSensitivity: Double,
    val volume: Long = 0L,
    val high: Long = 0L,
    val low: Long = 0L,
    val open: Long = 0L,
    val status: StockStatus = StockStatus.LISTED,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(stockId.isNotBlank()) { "stockId must not be blank" }
        require(stockName.isNotBlank()) { "stockName must not be blank" }
        require(basePrice > 0) { "basePrice must be positive" }
        require(currentPrice >= 0) { "currentPrice must not be negative" }
        require(totalShares > 0) { "totalShares must be positive" }
    }

    val change: Long
        get() = currentPrice - previousClose

    val changePercent: Double
        get() = if (previousClose != 0L) {
            (currentPrice - previousClose).toDouble() / previousClose * 100
        } else {
            0.0
        }

    fun delist(): StockModel = copy(
        status = StockStatus.DELISTED,
        updatedAt = Instant.now()
    )

    fun updatePrice(newPrice: Long, newVolume: Long, newHigh: Long, newLow: Long): StockModel = copy(
        currentPrice = newPrice,
        volume = newVolume,
        high = newHigh,
        low = newLow,
        updatedAt = Instant.now()
    )

    companion object {
        fun create(
            stockId: String,
            stockName: String,
            sector: Sector,
            basePrice: Long,
            totalShares: Long,
            marketCapGrade: MarketCapGrade,
            volatility: Long = 0L,
            per: Double = 0.0,
            dividendRate: Double = 0.0,
            growthRate: Double = 0.0,
            eventSensitivity: Double = 0.0
        ): StockModel = StockModel(
            stockId = stockId,
            stockName = stockName,
            sector = sector,
            basePrice = basePrice,
            currentPrice = basePrice,
            previousClose = basePrice,
            totalShares = totalShares,
            marketCapGrade = marketCapGrade,
            volatility = volatility,
            per = per,
            dividendRate = dividendRate,
            growthRate = growthRate,
            eventSensitivity = eventSensitivity,
            volume = 0L,
            high = basePrice,
            low = basePrice,
            open = basePrice,
            status = StockStatus.LISTED
        )

        fun of(
            stockId: String,
            stockName: String,
            sector: Sector,
            basePrice: Long,
            currentPrice: Long,
            previousClose: Long,
            totalShares: Long,
            marketCapGrade: MarketCapGrade,
            volatility: Long,
            per: Double,
            dividendRate: Double,
            growthRate: Double,
            eventSensitivity: Double,
            volume: Long,
            high: Long,
            low: Long,
            open: Long,
            status: StockStatus,
            createdAt: Instant,
            updatedAt: Instant
        ): StockModel = StockModel(
            stockId = stockId,
            stockName = stockName,
            sector = sector,
            basePrice = basePrice,
            currentPrice = currentPrice,
            previousClose = previousClose,
            totalShares = totalShares,
            marketCapGrade = marketCapGrade,
            volatility = volatility,
            per = per,
            dividendRate = dividendRate,
            growthRate = growthRate,
            eventSensitivity = eventSensitivity,
            volume = volume,
            high = high,
            low = low,
            open = open,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}