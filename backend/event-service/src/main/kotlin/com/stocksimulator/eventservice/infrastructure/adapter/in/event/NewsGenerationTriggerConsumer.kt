package com.stocksimulator.eventservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.NewsGenerationTriggerEvent
import com.stocksimulator.eventservice.application.handler.news.GameNewsGenerationHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class NewsGenerationTriggerConsumer(
    private val gameNewsGenerationHandler: GameNewsGenerationHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.TRIGGER_NEWS_GENERATION], groupId = "event-service")
    fun handleNewsGenerationTrigger(event: NewsGenerationTriggerEvent, ack: Acknowledgment) {
        try {
            val level = try {
                EventLevel.valueOf(event.level)
            } catch (e: IllegalArgumentException) {
                log.warn("알 수 없는 뉴스 레벨: {}, 무시합니다.", event.level)
                ack.acknowledge()
                return
            }

            log.info("뉴스 생성 트리거 수신: level={}, eventId={}", level, event.eventId)
            runBlocking { gameNewsGenerationHandler.generateAndPublish(level) }
            log.info("뉴스 생성 처리 완료: level={}", level)
        } catch (e: Exception) {
            log.error("뉴스 생성 처리 실패: level={}, error={}", event.level, e.message, e)
        } finally {
            ack.acknowledge()
        }
    }
}
