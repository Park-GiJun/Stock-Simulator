package com.stocksimulator.eventservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.NewsPublishedEvent
import com.stocksimulator.eventservice.application.port.out.NewsEventPort
import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaNewsEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : NewsEventPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishNewsPublished(news: NewsArticleModel) {
        val event = NewsPublishedEvent(
            newsId = news.newsId,
            gameEventId = news.gameEventId,
            headline = news.headline,
            content = news.content,
            summary = news.summary,
            level = news.level.name,
            sentiment = news.sentiment.name,
            intensity = news.intensity,
            duration = news.duration,
            sector = news.sector,
            stockId = news.stockId,
            publishedAt = news.publishedAt
        )
        kafkaTemplate.send(KafkaTopics.NEWS_PUBLISHED, news.newsId, event)
        log.debug("뉴스 발행 이벤트 전송: newsId={}, headline={}", news.newsId, news.headline)
    }
}
