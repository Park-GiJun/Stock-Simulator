package com.stocksimulator.tradingservice.application.port.out.portfolio

import com.stocksimulator.tradingservice.domain.model.PortfolioModel

interface PortfolioPersistencePort {
    fun save(portfolio: PortfolioModel): PortfolioModel
    fun saveAll(portfolios: List<PortfolioModel>): List<PortfolioModel>
    fun findByInvestorAndStock(investorId: String, investorType: String, stockId: String): PortfolioModel?
}
