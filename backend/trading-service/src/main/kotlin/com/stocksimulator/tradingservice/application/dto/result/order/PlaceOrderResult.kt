package com.stocksimulator.tradingservice.application.dto.result.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.domain.vo.MatchResult
import com.stocksimulator.tradingservice.domain.model.OrderModel

data class PlaceOrderResult(
    val orderId: String,
    val userId: String,
    val stockId: String,
    val orderType: OrderType,
    val orderKind: OrderKind,
    val price: Long?,
    val quantity: Long,
    val filledQuantity: Long,
    val status: OrderStatus,
    val matches: List<MatchDetail>
) {
    data class MatchDetail(
        val tradeId: String,
        val price: Long,
        val quantity: Long
    )

    companion object {
        fun from(order: OrderModel, matchResults: List<MatchResult>): PlaceOrderResult {
            return PlaceOrderResult(
                orderId = order.orderId,
                userId = order.userId,
                stockId = order.stockId,
                orderType = order.orderType,
                orderKind = order.orderKind,
                price = order.price,
                quantity = order.quantity,
                filledQuantity = order.filledQuantity,
                status = order.status,
                matches = matchResults.map { MatchDetail(it.tradeId, it.price, it.quantity) }
            )
        }
    }
}
