package com.stocksimulator.tradingservice.application.port.out.order

import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.tradingservice.domain.model.OrderModel

interface OrderPersistencePort {
    fun save(order: OrderModel): OrderModel
    fun saveAll(orders: List<OrderModel>): List<OrderModel>
    fun findById(orderId: String): OrderModel?
    fun update(order: OrderModel): OrderModel
    fun findByStockIdAndStatusIn(stockId: String, statuses: List<OrderStatus>): List<OrderModel>
    fun findByStockId(stockId: String): List<OrderModel>
    fun updateRemainingQuantities(entries: Map<String, Long>)
}
