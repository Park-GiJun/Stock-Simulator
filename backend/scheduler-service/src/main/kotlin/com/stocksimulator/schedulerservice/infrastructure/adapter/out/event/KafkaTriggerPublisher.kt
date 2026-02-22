package com.stocksimulator.schedulerservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.*
import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaTriggerPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : TriggerPublishPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishStockListingTrigger() {
        val event = StockListingTriggerEvent()
        log.info("주식 상장 트리거 이벤트 발행: {}", event.eventId)
        kafkaTemplate.send(KafkaTopics.TRIGGER_STOCK_LISTING, event.eventId, event)
    }

    override fun publishStockDelistingTrigger() {
        val event = StockDelistingTriggerEvent()
        log.info("주식 상장폐지 트리거 이벤트 발행: {}", event.eventId)
        kafkaTemplate.send(KafkaTopics.TRIGGER_STOCK_DELISTING, event.eventId, event)
    }

    override fun publishNpcCreationTrigger(count: Int) {
        val event = NpcCreationTriggerEvent(count = count)
        log.info("NPC 생성 트리거 이벤트 발행: count={}, eventId={}", count, event.eventId)
        kafkaTemplate.send(KafkaTopics.TRIGGER_NPC_CREATION, event.eventId, event)
    }

    override fun publishInstitutionCreationTrigger() {
        val event = InstitutionCreationTriggerEvent()
        log.info("기관투자자 생성 트리거 이벤트 발행: {}", event.eventId)
        kafkaTemplate.send(KafkaTopics.TRIGGER_INSTITUTION_CREATION, event.eventId, event)
    }

    override fun publishNewsGenerationTrigger(level: String) {
        val event = NewsGenerationTriggerEvent(level = level)
        log.info("뉴스 생성 트리거 이벤트 발행: level={}, eventId={}", level, event.eventId)
        kafkaTemplate.send(KafkaTopics.TRIGGER_NEWS_GENERATION, event.eventId, event)
    }
}
