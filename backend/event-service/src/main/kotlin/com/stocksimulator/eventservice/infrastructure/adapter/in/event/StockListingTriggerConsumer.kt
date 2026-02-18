package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.StockListingTriggerEvent
import com.stocksimulator.eventservice.application.handler.stock.StockListingHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class StockListingTriggerConsumer(
    private val stockListingHandler: StockListingHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_STOCK_LISTING], groupId = "event-service")
    fun handleStockListingTrigger(event: StockListingTriggerEvent, ack: Acknowledgment) {
        log.info("주식 상장 트리거 수신 - triggeredAt: {}", event.triggeredAt)
        try {
            runBlocking {
                stockListingHandler.initiateIPO()
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("주식 상장 처리 실패: {}", e.message, e)
            ack.acknowledge()
        }
    }
}
