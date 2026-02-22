package com.stocksimulator.eventservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.eventservice.application.dto.result.NewsArticleResult
import com.stocksimulator.eventservice.application.dto.result.NewsListResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "뉴스 기사 응답")
data class NewsArticleResponse(
    @Schema(description = "뉴스 ID")
    val newsId: String,

    @Schema(description = "게임 이벤트 ID")
    val gameEventId: String?,

    @Schema(description = "헤드라인")
    val headline: String,

    @Schema(description = "본문")
    val content: String,

    @Schema(description = "요약")
    val summary: String,

    @Schema(description = "레벨 (SOCIETY/INDUSTRY/COMPANY)")
    val level: String,

    @Schema(description = "감정 (POSITIVE/NEGATIVE/NEUTRAL)")
    val sentiment: String,

    @Schema(description = "영향도 (0.0~1.0)")
    val intensity: Double,

    @Schema(description = "영향 지속 시간 (게임시간 분)")
    val duration: Long,

    @Schema(description = "섹터")
    val sector: String?,

    @Schema(description = "종목 ID")
    val stockId: String?,

    @Schema(description = "소스 타입 (IPO/DELISTING/GAME_EVENT)")
    val sourceType: String,

    @Schema(description = "발행 시각")
    val publishedAt: Instant,

    @Schema(description = "생성 시각")
    val createdAt: Instant
) {
    companion object {
        fun from(result: NewsArticleResult): NewsArticleResponse {
            return NewsArticleResponse(
                newsId = result.newsId,
                gameEventId = result.gameEventId,
                headline = result.headline,
                content = result.content,
                summary = result.summary,
                level = result.level,
                sentiment = result.sentiment,
                intensity = result.intensity,
                duration = result.duration,
                sector = result.sector,
                stockId = result.stockId,
                sourceType = result.sourceType,
                publishedAt = result.publishedAt,
                createdAt = result.createdAt
            )
        }
    }
}

@Schema(description = "뉴스 목록 응답")
data class NewsListResponse(
    @Schema(description = "뉴스 목록")
    val news: List<NewsArticleResponse>,

    @Schema(description = "현재 페이지")
    val page: Int,

    @Schema(description = "페이지 크기")
    val size: Int,

    @Schema(description = "전체 건수")
    val totalElements: Long,

    @Schema(description = "전체 페이지 수")
    val totalPages: Int
) {
    companion object {
        fun from(result: NewsListResult): NewsListResponse {
            return NewsListResponse(
                news = result.news.map { NewsArticleResponse.from(it) },
                page = result.page,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages
            )
        }
    }
}
