package com.stocksimulator.eventservice.application.dto.result

import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import java.time.Instant

data class NewsArticleResult(
    val newsId: String,
    val gameEventId: String?,
    val headline: String,
    val content: String,
    val summary: String,
    val level: String,
    val sentiment: String,
    val intensity: Double,
    val duration: Long,
    val sector: String?,
    val stockId: String?,
    val sourceType: String,
    val publishedAt: Instant,
    val createdAt: Instant
) {
    companion object {
        fun from(model: NewsArticleModel): NewsArticleResult {
            return NewsArticleResult(
                newsId = model.newsId,
                gameEventId = model.gameEventId,
                headline = model.headline,
                content = model.content,
                summary = model.summary,
                level = model.level.name,
                sentiment = model.sentiment.name,
                intensity = model.intensity,
                duration = model.duration,
                sector = model.sector,
                stockId = model.stockId,
                sourceType = model.sourceType.name,
                publishedAt = model.publishedAt,
                createdAt = model.createdAt
            )
        }
    }
}

data class NewsListResult(
    val news: List<NewsArticleResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
