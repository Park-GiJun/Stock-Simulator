package com.stocksimulator.tradingservice.domain.model

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.dto.OrderType
import java.time.Instant
import java.util.UUID

data class OrderModel(
    val orderId: String,
    val userId: String,
    val stockId: String,
    val orderType: OrderType,
    val orderKind: OrderKind,
    val price: Long?,
    val quantity: Long,
    val filledQuantity: Long,
    val status: OrderStatus,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    val remainingQuantity: Long
        get() = quantity - filledQuantity

    init {
        require(orderId.isNotBlank()) { "orderId must not be blank" }
        require(userId.isNotBlank()) { "userId must not be blank" }
        require(stockId.isNotBlank()) { "stockId must not be blank" }
        require(quantity > 0) { "quantity must be positive" }
        require(filledQuantity >= 0) { "filledQuantity must not be negative" }
        require(filledQuantity <= quantity) { "filledQuantity must not exceed quantity" }
        if (orderKind == OrderKind.LIMIT) {
            requireNotNull(price) { "LIMIT order must have a price" }
            require(price > 0) { "price must be positive" }
        }
        if (orderKind == OrderKind.MARKET) {
            require(price == null) { "MARKET order must not have a price" }
        }
    }

    fun fill(matchedQuantity: Long): OrderModel {
        require(matchedQuantity > 0) { "matchedQuantity must be positive" }
        val newFilledQuantity = filledQuantity + matchedQuantity
        require(newFilledQuantity <= quantity) { "Cannot fill more than order quantity" }
        val newStatus = if (newFilledQuantity == quantity) OrderStatus.FILLED else OrderStatus.PARTIALLY_FILLED
        return copy(
            filledQuantity = newFilledQuantity,
            status = newStatus,
            updatedAt = Instant.now()
        )
    }

    fun cancel(): OrderModel {
        require(status == OrderStatus.PENDING || status == OrderStatus.PARTIALLY_FILLED) {
            "Only PENDING or PARTIALLY_FILLED orders can be cancelled"
        }
        return copy(status = OrderStatus.CANCELLED, updatedAt = Instant.now())
    }

    fun reject(): OrderModel {
        return copy(status = OrderStatus.REJECTED, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            userId: String,
            stockId: String,
            orderType: OrderType,
            orderKind: OrderKind,
            price: Long?,
            quantity: Long
        ): OrderModel {
            return OrderModel(
                orderId = UUID.randomUUID().toString(),
                userId = userId,
                stockId = stockId,
                orderType = orderType,
                orderKind = orderKind,
                price = price,
                quantity = quantity,
                filledQuantity = 0,
                status = OrderStatus.PENDING
            )
        }

        fun of(
            orderId: String,
            userId: String,
            stockId: String,
            orderType: OrderType,
            orderKind: OrderKind,
            price: Long?,
            quantity: Long,
            filledQuantity: Long,
            status: OrderStatus,
            createdAt: Instant,
            updatedAt: Instant = Instant.now()
        ): OrderModel {
            return OrderModel(
                orderId = orderId,
                userId = userId,
                stockId = stockId,
                orderType = orderType,
                orderKind = orderKind,
                price = price,
                quantity = quantity,
                filledQuantity = filledQuantity,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}
