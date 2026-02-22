package com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.entity

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sentiment
import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import com.stocksimulator.eventservice.domain.model.NewsSourceType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "news_articles",
    schema = "events",
    indexes = [
        Index(name = "idx_news_stock_id", columnList = "stock_id"),
        Index(name = "idx_news_sector", columnList = "sector"),
        Index(name = "idx_news_level", columnList = "level"),
        Index(name = "idx_news_sentiment", columnList = "sentiment"),
        Index(name = "idx_news_published_at", columnList = "published_at"),
        Index(name = "idx_news_source_type", columnList = "source_type")
    ]
)
class NewsJpaEntity(
    @Id
    @Column(name = "news_id", length = 36)
    val newsId: String,

    @Column(name = "game_event_id", length = 36, nullable = true)
    val gameEventId: String?,

    @Column(name = "headline", nullable = false, length = 500)
    val headline: String,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "summary", nullable = false, length = 500)
    val summary: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    val level: EventLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false, length = 20)
    val sentiment: Sentiment,

    @Column(name = "intensity", nullable = false)
    val intensity: Double,

    @Column(name = "duration", nullable = false)
    val duration: Long,

    @Column(name = "sector", nullable = true, length = 50)
    val sector: String?,

    @Column(name = "stock_id", nullable = true, length = 20)
    val stockId: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    val sourceType: NewsSourceType,

    @Column(name = "published_at", nullable = false)
    val publishedAt: Instant,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    fun toDomain(): NewsArticleModel {
        return NewsArticleModel(
            newsId = newsId,
            gameEventId = gameEventId,
            headline = headline,
            content = content,
            summary = summary,
            level = level,
            sentiment = sentiment,
            intensity = intensity,
            duration = duration,
            sector = sector,
            stockId = stockId,
            sourceType = sourceType,
            publishedAt = publishedAt,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: NewsArticleModel): NewsJpaEntity {
            return NewsJpaEntity(
                newsId = domain.newsId,
                gameEventId = domain.gameEventId,
                headline = domain.headline,
                content = domain.content,
                summary = domain.summary,
                level = domain.level,
                sentiment = domain.sentiment,
                intensity = domain.intensity,
                duration = domain.duration,
                sector = domain.sector,
                stockId = domain.stockId,
                sourceType = domain.sourceType,
                publishedAt = domain.publishedAt,
                createdAt = domain.createdAt
            )
        }
    }
}
