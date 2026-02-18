package com.stocksimulator.tradingservice.application.dto.result.portfolio

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.PortfolioModel

data class PortfolioResult(
    val investorId: String,
    val investorType: TradingInvestorType,
    val stockId: String,
    val quantity: Long,
    val averagePrice: Long,
    val totalInvested: Long
) {
    companion object {
        fun from(model: PortfolioModel): PortfolioResult = PortfolioResult(
            investorId = model.investorId,
            investorType = model.investorType,
            stockId = model.stockId,
            quantity = model.quantity,
            averagePrice = model.averagePrice,
            totalInvested = model.totalInvested
        )
    }
}
