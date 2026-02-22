package com.stocksimulator.eventservice.application.port.out.trading

interface TradingDecisionGeneratePort {
    suspend fun generateDecision(request: TradingDecisionRequest): TradingDecisionResult
}

data class TradingDecisionRequest(
    val investorName: String,
    val investmentStyle: String,
    val riskTolerance: Double,
    val availableCash: Long,
    val preferredSectors: List<String>,
    val currentHoldings: List<PortfolioHoldingDto>,
    val stockCandidates: List<StockCandidateDto>,
    val recentNews: List<NewsInfoDto>
)

data class NewsInfoDto(
    val headline: String,
    val sentiment: String,
    val sector: String?
)

data class TradingDecisionResult(
    val action: String,  // BUY, SELL, HOLD
    val stockId: String?,
    val quantity: Long
)
