package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.dto.result.portfolio.TradeResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "체결 이력 응답")
data class TradeResponse(
    @Schema(description = "체결 ID")
    val tradeId: String,
    @Schema(description = "매수 주문 ID")
    val buyOrderId: String,
    @Schema(description = "매도 주문 ID")
    val sellOrderId: String,
    @Schema(description = "매수자 ID")
    val buyerId: String,
    @Schema(description = "매수자 유형")
    val buyerType: TradingInvestorType,
    @Schema(description = "매도자 ID")
    val sellerId: String,
    @Schema(description = "매도자 유형")
    val sellerType: TradingInvestorType,
    @Schema(description = "종목 ID")
    val stockId: String,
    @Schema(description = "체결 가격")
    val price: Long,
    @Schema(description = "체결 수량")
    val quantity: Long,
    @Schema(description = "체결 금액")
    val tradeAmount: Long,
    @Schema(description = "체결 시각")
    val tradedAt: String
) {
    companion object {
        fun from(result: TradeResult): TradeResponse = TradeResponse(
            tradeId = result.tradeId,
            buyOrderId = result.buyOrderId,
            sellOrderId = result.sellOrderId,
            buyerId = result.buyerId,
            buyerType = result.buyerType,
            sellerId = result.sellerId,
            sellerType = result.sellerType,
            stockId = result.stockId,
            price = result.price,
            quantity = result.quantity,
            tradeAmount = result.tradeAmount,
            tradedAt = result.tradedAt
        )
    }
}
