package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.NpcCreationTriggerEvent
import com.stocksimulator.eventservice.application.handler.npc.NpcCreationHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class NpcCreationTriggerConsumer(
    private val npcCreationHandler: NpcCreationHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_NPC_CREATION], groupId = "event-service")
    fun handleNpcCreationTrigger(event: NpcCreationTriggerEvent, ack: Acknowledgment) {
        log.info("NPC 생성 트리거 수신 - count: {}, triggeredAt: {}", event.count, event.triggeredAt)
        try {
            runBlocking {
                repeat(event.count) { index ->
                    try {
                        npcCreationHandler.createNpc()
                    } catch (e: Exception) {
                        log.error("NPC 생성 실패 ({}/{}): {}", index + 1, event.count, e.message, e)
                    }
                }
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("NPC 생성 트리거 처리 실패: {}", e.message, e)
            ack.acknowledge()
        }
    }
}
