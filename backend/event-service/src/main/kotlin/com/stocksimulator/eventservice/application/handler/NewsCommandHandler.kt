package com.stocksimulator.eventservice.application.handler

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sentiment
import com.stocksimulator.common.event.EventOccurredEvent
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.eventservice.application.port.`in`.PublishNewsUseCase
import com.stocksimulator.eventservice.application.port.out.NewsEventPort
import com.stocksimulator.eventservice.application.port.out.NewsPersistencePort
import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import com.stocksimulator.eventservice.domain.model.NewsSourceType
import com.stocksimulator.eventservice.domain.service.NewsContentGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NewsCommandHandler(
    private val newsPersistencePort: NewsPersistencePort,
    private val newsEventPort: NewsEventPort,
    private val newsContentGenerator: NewsContentGenerator
) : PublishNewsUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun publishIpoNews(event: StockListedEvent) {
        val headline = newsContentGenerator.generateIpoHeadline(event.stockName, event.sector)
        val content = newsContentGenerator.generateIpoContent(
            event.stockName, event.sector, event.basePrice, event.totalShares, event.marketCapGrade
        )
        val summary = newsContentGenerator.generateIpoSummary(event.stockName, event.sector)

        val news = NewsArticleModel(
            gameEventId = null,
            headline = headline,
            content = content,
            summary = summary,
            level = EventLevel.COMPANY,
            sentiment = Sentiment.POSITIVE,
            intensity = 0.6,
            duration = 120,
            sector = event.sector,
            stockId = event.stockId,
            sourceType = NewsSourceType.IPO
        )

        val saved = newsPersistencePort.save(news)
        newsEventPort.publishNewsPublished(saved)
        log.info("IPO 뉴스 발행: newsId={}, stockId={}, headline={}", saved.newsId, saved.stockId, saved.headline)
    }

    override suspend fun publishDelistingNews(event: StockDelistedEvent) {
        val headline = newsContentGenerator.generateDelistingHeadline(event.stockName)
        val content = newsContentGenerator.generateDelistingContent(event.stockName, event.reason, event.finalPrice)
        val summary = newsContentGenerator.generateDelistingSummary(event.stockName)

        val news = NewsArticleModel(
            gameEventId = null,
            headline = headline,
            content = content,
            summary = summary,
            level = EventLevel.COMPANY,
            sentiment = Sentiment.NEGATIVE,
            intensity = 0.9,
            duration = 240,
            sector = null,
            stockId = event.stockId,
            sourceType = NewsSourceType.DELISTING
        )

        val saved = newsPersistencePort.save(news)
        newsEventPort.publishNewsPublished(saved)
        log.info("상장폐지 뉴스 발행: newsId={}, stockId={}, headline={}", saved.newsId, saved.stockId, saved.headline)
    }

    override suspend fun publishGameEventNews(event: EventOccurredEvent) {
        val level = try {
            EventLevel.valueOf(event.level)
        } catch (e: IllegalArgumentException) {
            EventLevel.SOCIETY
        }

        val news = NewsArticleModel(
            gameEventId = event.gameEventId,
            headline = event.headline,
            content = event.content,
            summary = event.headline,
            level = level,
            sentiment = event.sentiment,
            intensity = event.intensity,
            duration = event.duration,
            sector = event.targetSector,
            stockId = event.targetStockId,
            sourceType = NewsSourceType.GAME_EVENT
        )

        val saved = newsPersistencePort.save(news)
        newsEventPort.publishNewsPublished(saved)
        log.info("게임이벤트 뉴스 발행: newsId={}, gameEventId={}, headline={}", saved.newsId, saved.gameEventId, saved.headline)
    }
}
