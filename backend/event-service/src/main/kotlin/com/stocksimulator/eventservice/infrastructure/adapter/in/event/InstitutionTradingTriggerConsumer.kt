package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.InstitutionTradingTriggerEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.eventservice.application.handler.trading.InstitutionTradingHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class InstitutionTradingTriggerConsumer(
    private val institutionTradingHandler: InstitutionTradingHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_INSTITUTION_TRADING], groupId = "event-service")
    fun handleInstitutionTradingTrigger(event: InstitutionTradingTriggerEvent, ack: Acknowledgment) {
        log.info("기관투자자 자동매매 트리거 수신: eventId={}", event.eventId)
        try {
            runBlocking {
                institutionTradingHandler.handleInstitutionTrading()
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("기관투자자 자동매매 트리거 처리 실패: {}", e.message, e)
            ack.acknowledge()
        }
    }
}
