package com.stocksimulator.eventservice.application.handler.news

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.EventOccurredEvent
import com.stocksimulator.eventservice.application.port.out.NewsEventPort
import com.stocksimulator.eventservice.application.port.out.news.NewsContentGeneratePort
import com.stocksimulator.eventservice.application.port.out.stock.StockQueryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GameNewsGenerationHandler(
    private val newsContentGeneratePort: NewsContentGeneratePort,
    private val newsEventPort: NewsEventPort,
    private val stockQueryPort: StockQueryPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun generateAndPublish(level: EventLevel) {
        val sector = when (level) {
            EventLevel.COMPANY, EventLevel.INDUSTRY -> Sector.entries.random()
            EventLevel.SOCIETY -> null
        }

        var stockName: String? = null
        var stockId: String? = null

        if (level == EventLevel.COMPANY) {
            try {
                val stock = stockQueryPort.getRandomListedStock()
                if (stock != null) {
                    stockName = stock.stockName
                    stockId = stock.stockId
                }
            } catch (e: Exception) {
                log.warn("랜덤 종목 조회 실패, 종목명 없이 진행: {}", e.message)
            }
        }

        log.info("뉴스 생성 시작: level={}, sector={}, stockName={}", level, sector?.name, stockName)

        val generated = newsContentGeneratePort.generateNews(level, sector, stockName)

        val event = EventOccurredEvent(
            gameEventId = UUID.randomUUID().toString(),
            level = level.name,
            targetSector = sector?.name,
            targetStockId = stockId,
            sentiment = generated.sentiment,
            intensity = generated.intensity,
            duration = generated.duration,
            headline = generated.headline,
            content = generated.content
        )

        newsEventPort.publishEventOccurred(event)

        log.info(
            "뉴스 생성 완료 및 event.occurred 발행: gameEventId={}, level={}, sector={}, stockName={}, sentiment={}, headline={}",
            event.gameEventId, level, sector?.name, stockName, generated.sentiment, generated.headline
        )
    }
}
