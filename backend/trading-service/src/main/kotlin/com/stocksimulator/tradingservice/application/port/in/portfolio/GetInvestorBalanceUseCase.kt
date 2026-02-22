package com.stocksimulator.tradingservice.application.port.`in`.portfolio

import com.stocksimulator.tradingservice.application.dto.query.portfolio.InvestorBalanceQuery
import com.stocksimulator.tradingservice.application.dto.result.portfolio.InvestorBalanceResult

interface GetInvestorBalanceUseCase {
    fun getBalance(query: InvestorBalanceQuery): InvestorBalanceResult
}
