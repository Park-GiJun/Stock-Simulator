package com.stocksimulator.stockservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.stockservice.application.dto.command.stock.CreateStockCommand
import com.stocksimulator.stockservice.application.port.`in`.stock.CreateStockUseCase
import com.stocksimulator.stockservice.application.port.`in`.stock.DelistStockUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class StockEventConsumer(
    private val createStockUseCase: CreateStockUseCase,
    private val delistStockUseCase: DelistStockUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.STOCK_LISTED], groupId = "stock-service")
    fun handleStockListed(event: StockListedEvent, ack: Acknowledgment) {
        try {
            log.info("Received stock.listed event: stockId={}, stockName={}", event.stockId, event.stockName)

            val command = CreateStockCommand(
                stockId = event.stockId,
                stockName = event.stockName,
                sector = Sector.valueOf(event.sector),
                basePrice = event.basePrice,
                totalShares = event.totalShares,
                marketCapGrade = MarketCapGrade.valueOf(event.marketCapGrade),
                volatility = event.volatility,
                per = event.per,
                dividendRate = event.dividendRate,
                growthRate = event.growthRate,
                eventSensitivity = event.eventSensitivity
            )
            createStockUseCase.createStock(command)

            log.info("Stock listed successfully: {}", event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle stock.listed event: stockId={}", event.stockId, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.STOCK_DELISTED], groupId = "stock-service")
    fun handleStockDelisted(event: StockDelistedEvent, ack: Acknowledgment) {
        try {
            log.info("Received stock.delisted event: stockId={}, reason={}", event.stockId, event.reason)

            delistStockUseCase.delistStock(event.stockId)

            log.info("Stock delisted successfully: {}", event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle stock.delisted event: stockId={}", event.stockId, e)
            ack.acknowledge()
        }
    }
}
