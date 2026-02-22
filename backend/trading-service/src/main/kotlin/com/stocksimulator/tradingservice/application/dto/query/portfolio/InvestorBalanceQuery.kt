package com.stocksimulator.tradingservice.application.dto.query.portfolio

import com.stocksimulator.common.dto.TradingInvestorType

data class InvestorBalanceQuery(
    val investorId: String,
    val investorType: TradingInvestorType = TradingInvestorType.USER
)
