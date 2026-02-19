package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.order.entity

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.domain.model.OrderModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "orders",
    schema = "trading",
    indexes = [
        Index(name = "idx_orders_user_id", columnList = "user_id"),
        Index(name = "idx_orders_stock_id", columnList = "stock_id"),
        Index(name = "idx_orders_status", columnList = "status"),
        Index(name = "idx_orders_created_at", columnList = "created_at")
    ]
)
class OrderJpaEntity(
    @Id
    @Column(name = "order_id", length = 36)
    val orderId: String,

    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,

    @Column(name = "stock_id", nullable = false, length = 20)
    val stockId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    val orderType: OrderType,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_kind", nullable = false, length = 10)
    val orderKind: OrderKind,

    @Column(name = "price")
    val price: Long?,

    @Column(name = "quantity", nullable = false)
    val quantity: Long,

    @Column(name = "filled_quantity", nullable = false)
    var filledQuantity: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: OrderStatus,

    @Column(name = "investor_type", nullable = false, length = 20)
    val investorType: String = "USER",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): OrderModel = OrderModel.of(
        orderId = orderId,
        userId = userId,
        stockId = stockId,
        orderType = orderType,
        orderKind = orderKind,
        price = price,
        quantity = quantity,
        filledQuantity = filledQuantity,
        status = status,
        investorType = investorType,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFromDomain(domain: OrderModel) {
        filledQuantity = domain.filledQuantity
        status = domain.status
        updatedAt = Instant.now()
    }

    companion object {
        fun fromDomain(domain: OrderModel): OrderJpaEntity = OrderJpaEntity(
            orderId = domain.orderId,
            userId = domain.userId,
            stockId = domain.stockId,
            orderType = domain.orderType,
            orderKind = domain.orderKind,
            price = domain.price,
            quantity = domain.quantity,
            filledQuantity = domain.filledQuantity,
            status = domain.status,
            investorType = domain.investorType,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
