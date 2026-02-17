package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주문 접수 요청")
data class PlaceOrderRequest(
    @Schema(description = "사용자 ID", example = "user-001", required = true)
    val userId: String,

    @Schema(description = "종목 ID", example = "STK001", required = true)
    val stockId: String,

    @Schema(description = "주문 유형 (BUY/SELL)", example = "BUY", required = true)
    val orderType: OrderType,

    @Schema(description = "주문 종류 (LIMIT/MARKET)", example = "LIMIT", required = true)
    val orderKind: OrderKind,

    @Schema(description = "주문 가격 (시장가인 경우 null)", example = "50000")
    val price: Long?,

    @Schema(description = "주문 수량", example = "10", required = true)
    val quantity: Long
)
