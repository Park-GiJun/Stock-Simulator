package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.dto.result.portfolio.InvestorBalanceResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔고 응답")
data class InvestorBalanceResponse(
    @Schema(description = "투자자 ID")
    val investorId: String,
    @Schema(description = "투자자 유형")
    val investorType: TradingInvestorType,
    @Schema(description = "현금 잔고")
    val cash: Long
) {
    companion object {
        fun from(result: InvestorBalanceResult): InvestorBalanceResponse = InvestorBalanceResponse(
            investorId = result.investorId,
            investorType = result.investorType,
            cash = result.cash
        )
    }
}
