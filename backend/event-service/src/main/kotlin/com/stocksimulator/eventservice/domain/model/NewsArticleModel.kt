package com.stocksimulator.eventservice.domain.model

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sentiment
import java.time.Instant
import java.util.UUID

data class NewsArticleModel(
    val newsId: String = UUID.randomUUID().toString(),
    val gameEventId: String?,
    val headline: String,
    val content: String,
    val summary: String,
    val level: EventLevel,
    val sentiment: Sentiment,
    val intensity: Double,
    val duration: Long,
    val sector: String?,
    val stockId: String?,
    val sourceType: NewsSourceType,
    val publishedAt: Instant = Instant.now(),
    val createdAt: Instant = Instant.now()
)
