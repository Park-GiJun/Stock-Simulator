package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.StockDelistingTriggerEvent
import com.stocksimulator.eventservice.application.handler.stock.StockListingHandler
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class StockDelistingTriggerConsumer(
    private val stockListingHandler: StockListingHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_STOCK_DELISTING], groupId = "event-service")
    fun handleStockDelistingTrigger(event: StockDelistingTriggerEvent, ack: Acknowledgment) {
        log.info("주식 상장폐지 트리거 수신 - triggeredAt: {}", event.triggeredAt)
        try {
            stockListingHandler.initiateDelisting()
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("주식 상장폐지 처리 실패: {}", e.message, e)
            ack.acknowledge()
        }
    }
}
