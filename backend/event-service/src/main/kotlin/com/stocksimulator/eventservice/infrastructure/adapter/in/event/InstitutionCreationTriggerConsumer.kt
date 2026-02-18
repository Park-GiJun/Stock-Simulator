package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.InstitutionCreationTriggerEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.eventservice.application.handler.institution.InstitutionCreationHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class InstitutionCreationTriggerConsumer(
    private val institutionCreationHandler: InstitutionCreationHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_INSTITUTION_CREATION], groupId = "event-service")
    fun handleInstitutionCreationTrigger(event: InstitutionCreationTriggerEvent, ack: Acknowledgment) {
        log.info("기관투자자 생성 트리거 수신 - triggeredAt: {}", event.triggeredAt)
        try {
            runBlocking {
                institutionCreationHandler.createInstitution()
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("기관투자자 생성 처리 실패: {}", e.message, e)
            ack.acknowledge()
        }
    }
}
