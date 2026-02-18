package com.stocksimulator.tradingservice.application.dto.result.portfolio

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel

data class InvestorBalanceResult(
    val investorId: String,
    val investorType: TradingInvestorType,
    val cash: Long
) {
    companion object {
        fun from(model: InvestorBalanceModel): InvestorBalanceResult = InvestorBalanceResult(
            investorId = model.investorId,
            investorType = model.investorType,
            cash = model.cash
        )
    }
}
