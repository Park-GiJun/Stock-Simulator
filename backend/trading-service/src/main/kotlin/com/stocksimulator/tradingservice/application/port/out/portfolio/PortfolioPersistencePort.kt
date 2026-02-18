package com.stocksimulator.tradingservice.application.port.out.portfolio

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.PortfolioModel

interface PortfolioPersistencePort {
    fun findByInvestorAndStock(investorId: String, investorType: TradingInvestorType, stockId: String): PortfolioModel?
    fun findByInvestor(investorId: String, investorType: TradingInvestorType): List<PortfolioModel>
    fun save(portfolio: PortfolioModel): PortfolioModel
}
