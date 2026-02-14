package com.stocksimulator.schedulerservice.adapter.out.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.schedulerservice.application.port.out.StockEventPublishPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaStockEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : StockEventPublishPort {

    override fun publishStockListed(event: StockListedEvent) {
        kafkaTemplate.send(KafkaTopics.STOCK_LISTED, event.stockId, event)
    }

    override fun publishStockDelisted(event: StockDelistedEvent) {
        kafkaTemplate.send(KafkaTopics.STOCK_DELISTED, event.stockId, event)
    }
}
