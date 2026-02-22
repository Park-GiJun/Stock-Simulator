package com.stocksimulator.eventservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.ScheduleTradeEvent
import com.stocksimulator.eventservice.application.port.out.trading.TradeEventPublishPort
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaTradeEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : TradeEventPublishPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishScheduleTrade(event: ScheduleTradeEvent) {
        log.info("스케줄 거래 이벤트 발행: investorId={}, stockId={}, orderType={}, quantity={}",
            event.investorId, event.stockId, event.orderType, event.quantity)
        kafkaTemplate.send(KafkaTopics.SCHEDULE_TRADE, event.eventId, event)
    }
}
