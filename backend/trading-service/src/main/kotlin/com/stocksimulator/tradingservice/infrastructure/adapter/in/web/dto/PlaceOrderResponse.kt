package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.application.dto.result.order.PlaceOrderResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주문 접수 응답")
data class PlaceOrderResponse(
    @Schema(description = "주문 ID")
    val orderId: String,

    @Schema(description = "사용자 ID")
    val userId: String,

    @Schema(description = "종목 ID")
    val stockId: String,

    @Schema(description = "주문 유형")
    val orderType: OrderType,

    @Schema(description = "주문 종류")
    val orderKind: OrderKind,

    @Schema(description = "주문 가격")
    val price: Long?,

    @Schema(description = "주문 수량")
    val quantity: Long,

    @Schema(description = "체결 수량")
    val filledQuantity: Long,

    @Schema(description = "주문 상태")
    val status: OrderStatus,

    @Schema(description = "매칭 내역")
    val matches: List<MatchDetailResponse>
) {
    @Schema(description = "매칭 상세")
    data class MatchDetailResponse(
        @Schema(description = "거래 ID")
        val tradeId: String,
        @Schema(description = "체결 가격")
        val price: Long,
        @Schema(description = "체결 수량")
        val quantity: Long
    )

    companion object {
        fun from(result: PlaceOrderResult): PlaceOrderResponse {
            return PlaceOrderResponse(
                orderId = result.orderId,
                userId = result.userId,
                stockId = result.stockId,
                orderType = result.orderType,
                orderKind = result.orderKind,
                price = result.price,
                quantity = result.quantity,
                filledQuantity = result.filledQuantity,
                status = result.status,
                matches = result.matches.map {
                    MatchDetailResponse(it.tradeId, it.price, it.quantity)
                }
            )
        }
    }
}
