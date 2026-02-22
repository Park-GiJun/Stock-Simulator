package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.order.adapter

import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.tradingservice.application.port.out.order.OrderPersistencePort
import com.stocksimulator.tradingservice.domain.model.OrderModel
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.order.entity.OrderJpaEntity
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.order.repository.OrderJpaRepository
import org.springframework.stereotype.Component

@Component
class OrderPersistenceAdapter(
    private val orderJpaRepository: OrderJpaRepository
) : OrderPersistencePort {

    override fun save(order: OrderModel): OrderModel {
        return orderJpaRepository.save(OrderJpaEntity.fromDomain(order)).toDomain()
    }

    override fun saveAll(orders: List<OrderModel>): List<OrderModel> {
        val entities = orders.map { OrderJpaEntity.fromDomain(it) }
        return orderJpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findById(orderId: String): OrderModel? {
        return orderJpaRepository.findByOrderId(orderId)?.toDomain()
    }

    override fun update(order: OrderModel): OrderModel {
        val entity = orderJpaRepository.findByOrderId(order.orderId)
            ?: return save(order)
        entity.updateFromDomain(order)
        return orderJpaRepository.save(entity).toDomain()
    }

    override fun findByStockIdAndStatusIn(stockId: String, statuses: List<OrderStatus>): List<OrderModel> {
        return orderJpaRepository.findByStockIdAndStatusIn(stockId, statuses)
            .map { it.toDomain() }
    }

    override fun updateRemainingQuantities(entries: Map<String, Long>) {
        if (entries.isEmpty()) return
        val orderIds = entries.keys.toList()
        val entities = orderJpaRepository.findAllById(orderIds).associateBy { it.orderId }
        for ((orderId, remainingQuantity) in entries) {
            val entity = entities[orderId] ?: continue
            val newFilledQuantity = entity.quantity - remainingQuantity
            if (entity.filledQuantity != newFilledQuantity) {
                entity.filledQuantity = newFilledQuantity
                entity.updatedAt = java.time.Instant.now()
            }
        }
    }
}
