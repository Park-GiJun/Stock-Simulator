package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.EventOccurredEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.eventservice.application.port.`in`.PublishNewsUseCase
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class NewsEventConsumer(
    private val publishNewsUseCase: PublishNewsUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.STOCK_LISTED], groupId = "event-service")
    fun handleStockListed(event: StockListedEvent, ack: Acknowledgment) {
        try {
            log.info("Received stock.listed event: stockId={}, stockName={}", event.stockId, event.stockName)
            runBlocking { publishNewsUseCase.publishIpoNews(event) }
            log.info("IPO news published successfully: stockId={}", event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle stock.listed event: stockId={}", event.stockId, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.STOCK_DELISTED], groupId = "event-service")
    fun handleStockDelisted(event: StockDelistedEvent, ack: Acknowledgment) {
        try {
            log.info("Received stock.delisted event: stockId={}, stockName={}", event.stockId, event.stockName)
            runBlocking { publishNewsUseCase.publishDelistingNews(event) }
            log.info("Delisting news published successfully: stockId={}", event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle stock.delisted event: stockId={}", event.stockId, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.EVENT_OCCURRED], groupId = "event-service")
    fun handleEventOccurred(event: EventOccurredEvent, ack: Acknowledgment) {
        try {
            log.info("Received event.occurred event: gameEventId={}, headline={}", event.gameEventId, event.headline)
            runBlocking { publishNewsUseCase.publishGameEventNews(event) }
            log.info("Game event news published successfully: gameEventId={}", event.gameEventId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle event.occurred event: gameEventId={}", event.gameEventId, e)
            ack.acknowledge()
        }
    }
}
