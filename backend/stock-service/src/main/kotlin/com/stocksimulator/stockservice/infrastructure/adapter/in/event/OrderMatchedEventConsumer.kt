package com.stocksimulator.stockservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.OrderMatchedEvent
import com.stocksimulator.stockservice.application.dto.command.stock.UpdateStockPriceCommand
import com.stocksimulator.stockservice.application.port.`in`.stock.UpdateStockPriceUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OrderMatchedEventConsumer(
    private val updateStockPriceUseCase: UpdateStockPriceUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.ORDER_MATCHED], groupId = "stock-service")
    fun handleOrderMatched(event: OrderMatchedEvent, ack: Acknowledgment) {
        try {
            log.info(
                "Received order.matched event: tradeId={}, stockId={}, price={}, quantity={}",
                event.tradeId, event.stockId, event.price, event.quantity
            )

            val command = UpdateStockPriceCommand(
                tradeId = event.tradeId,
                stockId = event.stockId,
                matchedPrice = event.price,
                matchedQuantity = event.quantity,
                matchedAt = event.matchedAt
            )
            updateStockPriceUseCase.updateStockPrice(command)

            log.info("Stock price updated from order match: tradeId={}, stockId={}", event.tradeId, event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle order.matched event: tradeId={}, stockId={}", event.tradeId, event.stockId, e)
            ack.acknowledge()
        }
    }
}
