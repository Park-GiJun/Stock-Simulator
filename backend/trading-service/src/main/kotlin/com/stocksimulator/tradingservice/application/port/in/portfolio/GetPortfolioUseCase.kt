package com.stocksimulator.tradingservice.application.port.`in`.portfolio

import com.stocksimulator.tradingservice.application.dto.query.portfolio.PortfolioQuery
import com.stocksimulator.tradingservice.application.dto.result.portfolio.PortfolioResult

interface GetPortfolioUseCase {
    fun getPortfolio(query: PortfolioQuery): List<PortfolioResult>
}
