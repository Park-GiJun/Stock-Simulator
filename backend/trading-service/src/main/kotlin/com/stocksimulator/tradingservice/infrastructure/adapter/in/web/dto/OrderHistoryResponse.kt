package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.OrderModel
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주문 내역 응답")
data class OrderHistoryResponse(
    @Schema(description = "주문 ID")
    val orderId: String,
    @Schema(description = "투자자 ID")
    val userId: String,
    @Schema(description = "투자자 유형")
    val investorType: TradingInvestorType,
    @Schema(description = "종목 ID")
    val stockId: String,
    @Schema(description = "주문 유형 (BUY/SELL)")
    val orderType: OrderType,
    @Schema(description = "주문 종류 (LIMIT/MARKET)")
    val orderKind: OrderKind,
    @Schema(description = "주문 가격")
    val price: Long?,
    @Schema(description = "주문 수량")
    val quantity: Long,
    @Schema(description = "체결 수량")
    val filledQuantity: Long,
    @Schema(description = "주문 상태")
    val status: OrderStatus,
    @Schema(description = "주문 시각")
    val createdAt: String
) {
    companion object {
        fun from(model: OrderModel): OrderHistoryResponse = OrderHistoryResponse(
            orderId = model.orderId,
            userId = model.userId,
            investorType = model.investorType,
            stockId = model.stockId,
            orderType = model.orderType,
            orderKind = model.orderKind,
            price = model.price,
            quantity = model.quantity,
            filledQuantity = model.filledQuantity,
            status = model.status,
            createdAt = model.createdAt.toString()
        )
    }
}
