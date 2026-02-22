package com.stocksimulator.stockservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.PriceUpdatedEvent
import com.stocksimulator.common.util.PriceUtil
import com.stocksimulator.stockservice.application.port.out.stock.StockEventPublishPort
import com.stocksimulator.stockservice.domain.model.StockModel
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaStockEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : StockEventPublishPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishPriceUpdated(stock: StockModel, previousPrice: Long, volume: Long) {
        val event = PriceUpdatedEvent(
            stockId = stock.stockId,
            previousPrice = previousPrice,
            currentPrice = stock.currentPrice,
            changeRate = PriceUtil.calculateChangeRate(previousPrice, stock.currentPrice),
            volume = volume
        )
        kafkaTemplate.send(KafkaTopics.PRICE_UPDATED, stock.stockId, event)
        log.debug("주가 변동 이벤트 발행: stockId={}, {} → {}", stock.stockId, previousPrice, stock.currentPrice)
    }
}
