package com.stocksimulator.eventservice.application.port.out.news

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.Sentiment

interface NewsContentGeneratePort {
    suspend fun generateNews(level: EventLevel, sector: Sector?): GeneratedNewsContent
}

data class GeneratedNewsContent(
    val headline: String,
    val content: String,
    val sentiment: Sentiment,
    val intensity: Double,
    val duration: Long
)
