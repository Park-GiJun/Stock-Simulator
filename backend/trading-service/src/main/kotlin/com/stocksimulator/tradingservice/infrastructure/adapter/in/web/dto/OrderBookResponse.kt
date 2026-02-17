package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.tradingservice.application.dto.result.order.OrderBookResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "호가창 조회 응답")
data class OrderBookResponse(
    @Schema(description = "종목 ID")
    val stockId: String,

    @Schema(description = "매수 호가")
    val bids: List<PriceLevelResponse>,

    @Schema(description = "매도 호가")
    val asks: List<PriceLevelResponse>,

    @Schema(description = "최우선 매수 호가")
    val bestBid: Long?,

    @Schema(description = "최우선 매도 호가")
    val bestAsk: Long?,

    @Schema(description = "스프레드")
    val spread: Long?
) {
    @Schema(description = "가격 레벨")
    data class PriceLevelResponse(
        @Schema(description = "가격")
        val price: Long,
        @Schema(description = "수량")
        val quantity: Long,
        @Schema(description = "주문 건수")
        val orderCount: Int
    )

    companion object {
        fun from(result: OrderBookResult): OrderBookResponse {
            return OrderBookResponse(
                stockId = result.stockId,
                bids = result.bids.map {
                    PriceLevelResponse(it.price, it.quantity, it.orderCount)
                },
                asks = result.asks.map {
                    PriceLevelResponse(it.price, it.quantity, it.orderCount)
                },
                bestBid = result.bestBid,
                bestAsk = result.bestAsk,
                spread = result.spread
            )
        }
    }
}
