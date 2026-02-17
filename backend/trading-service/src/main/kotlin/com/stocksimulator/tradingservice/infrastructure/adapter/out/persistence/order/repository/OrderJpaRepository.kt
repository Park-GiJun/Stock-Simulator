package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.order.repository

import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.order.entity.OrderJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderJpaEntity, String> {

    fun findByOrderId(orderId: String): OrderJpaEntity?

    fun findByStockIdAndStatusIn(stockId: String, statuses: List<OrderStatus>): List<OrderJpaEntity>

    fun findByUserIdAndStatusIn(userId: String, statuses: List<OrderStatus>): List<OrderJpaEntity>
}
