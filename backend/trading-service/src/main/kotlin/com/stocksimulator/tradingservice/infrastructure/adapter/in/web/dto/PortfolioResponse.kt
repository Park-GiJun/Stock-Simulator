package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.dto.result.portfolio.PortfolioResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "포트폴리오 응답")
data class PortfolioResponse(
    @Schema(description = "투자자 ID")
    val investorId: String,
    @Schema(description = "투자자 유형")
    val investorType: TradingInvestorType,
    @Schema(description = "보유종목 목록")
    val holdings: List<HoldingResponse>
) {
    companion object {
        fun from(results: List<PortfolioResult>): PortfolioResponse {
            val first = results.firstOrNull()
            return PortfolioResponse(
                investorId = first?.investorId ?: "",
                investorType = first?.investorType ?: TradingInvestorType.USER,
                holdings = results.map { HoldingResponse.from(it) }
            )
        }
    }
}

@Schema(description = "보유종목 정보")
data class HoldingResponse(
    @Schema(description = "종목 ID")
    val stockId: String,
    @Schema(description = "보유 수량")
    val quantity: Long,
    @Schema(description = "평균 매입가")
    val averagePrice: Long,
    @Schema(description = "총 투자 금액")
    val totalInvested: Long
) {
    companion object {
        fun from(result: PortfolioResult): HoldingResponse = HoldingResponse(
            stockId = result.stockId,
            quantity = result.quantity,
            averagePrice = result.averagePrice,
            totalInvested = result.totalInvested
        )
    }
}
