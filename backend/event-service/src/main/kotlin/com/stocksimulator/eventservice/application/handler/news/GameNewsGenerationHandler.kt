package com.stocksimulator.eventservice.application.handler.news

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.EventOccurredEvent
import com.stocksimulator.eventservice.application.port.out.NewsEventPort
import com.stocksimulator.eventservice.application.port.out.news.NewsContentGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GameNewsGenerationHandler(
    private val newsContentGeneratePort: NewsContentGeneratePort,
    private val newsEventPort: NewsEventPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun generateAndPublish(level: EventLevel) {
        val sector = when (level) {
            EventLevel.COMPANY, EventLevel.INDUSTRY -> Sector.entries.random()
            EventLevel.SOCIETY -> null
        }

        log.info("LLM 뉴스 생성 시작: level={}, sector={}", level, sector?.name)

        val generated = newsContentGeneratePort.generateNews(level, sector)

        val event = EventOccurredEvent(
            gameEventId = UUID.randomUUID().toString(),
            level = level.name,
            targetSector = sector?.name,
            targetStockId = null,
            sentiment = generated.sentiment,
            intensity = generated.intensity,
            duration = generated.duration,
            headline = generated.headline,
            content = generated.content
        )

        newsEventPort.publishEventOccurred(event)

        log.info(
            "LLM 뉴스 생성 완료 및 event.occurred 발행: gameEventId={}, level={}, sector={}, sentiment={}, headline={}",
            event.gameEventId, level, sector?.name, generated.sentiment, generated.headline
        )
    }
}
