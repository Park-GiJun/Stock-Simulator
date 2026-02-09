package com.stocksimulator.stockservice.application.dto.command.stock

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector

data class CreateStockCommand(
    val stockId: String,
    val stockName: String,
    val sector: Sector,
    val basePrice: Long,
    val totalShares: Long,
    val marketCapGrade: MarketCapGrade,
    val volatility: Long = 0L,
    val per: Double = 0.0,
    val dividendRate: Double = 0.0,
    val growthRate: Double = 0.0,
    val eventSensitivity: Double = 0.0
)
