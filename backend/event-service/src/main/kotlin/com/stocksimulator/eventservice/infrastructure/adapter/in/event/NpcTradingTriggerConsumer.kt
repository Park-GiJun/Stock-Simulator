package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.NpcTradingTriggerEvent
import com.stocksimulator.eventservice.application.handler.trading.NpcTradingHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class NpcTradingTriggerConsumer(
    private val npcTradingHandler: NpcTradingHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_NPC_TRADING], groupId = "event-service")
    fun handleNpcTradingTrigger(event: NpcTradingTriggerEvent, ack: Acknowledgment) {
        log.info("NPC 자동매매 트리거 수신 - frequency: {}, maxBatchSize: {}", event.tradingFrequency, event.maxBatchSize)
        try {
            runBlocking {
                npcTradingHandler.handleNpcTrading(event.tradingFrequency, event.maxBatchSize)
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("NPC 자동매매 트리거 처리 실패: {}", e.message, e)
            ack.acknowledge()
        }
    }
}
