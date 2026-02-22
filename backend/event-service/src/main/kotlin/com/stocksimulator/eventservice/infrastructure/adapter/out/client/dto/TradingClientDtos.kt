package com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto

data class NpcProfileResponse(
    val npcId: Long?,
    val npcName: String,
    val investmentStyle: String,
    val capital: Long,
    val weeklyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<String>,
    val tradingFrequency: String,
    val createdAt: String? = null
)

data class InstitutionProfileResponse(
    val institutionId: Long?,
    val institutionName: String,
    val institutionType: String,
    val investmentStyle: String,
    val capital: Long,
    val dailyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<String>,
    val tradingFrequency: String,
    val createdAt: String? = null
)

data class BalanceResponse(
    val investorId: String,
    val investorType: String,
    val cash: Long
)

data class PortfolioResponse(
    val investorId: String,
    val investorType: String,
    val holdings: List<PortfolioHoldingResponse>
)

data class PortfolioHoldingResponse(
    val stockId: String,
    val quantity: Long,
    val averagePrice: Long,
    val totalInvested: Long
)

data class StockListResponse(
    val stockId: String,
    val stockName: String,
    val sector: String,
    val currentPrice: Long,
    val change: Long = 0,
    val changePercent: Double = 0.0,
    val volume: Long = 0,
    val marketCapGrade: String? = null
)
