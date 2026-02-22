package com.stocksimulator.eventservice.infrastructure.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.eventservice.application.dto.query.NewsListQuery
import com.stocksimulator.eventservice.application.port.`in`.GetNewsUseCase
import com.stocksimulator.eventservice.infrastructure.adapter.`in`.web.dto.NewsArticleResponse
import com.stocksimulator.eventservice.infrastructure.adapter.`in`.web.dto.NewsListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/news")
@Tag(name = "News", description = "뉴스 API")
class NewsWebAdapter(
    private val getNewsUseCase: GetNewsUseCase
) {

    @GetMapping
    @Operation(summary = "뉴스 목록 조회")
    fun getNewsList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) level: String?,
        @RequestParam(required = false) sentiment: String?,
        @RequestParam(required = false) sector: String?,
        @RequestParam(required = false) stockId: String?
    ): Mono<ResponseEntity<ApiResponse<NewsListResponse>>> = mono {
        val query = NewsListQuery(
            page = page,
            size = size,
            level = level,
            sentiment = sentiment,
            sector = sector,
            stockId = stockId
        )
        val result = getNewsUseCase.getNewsList(query)
        ApiResponse.success(NewsListResponse.from(result)).toResponseEntity()
    }

    @GetMapping("/{newsId}")
    @Operation(summary = "뉴스 상세 조회")
    fun getNewsById(
        @PathVariable newsId: String
    ): Mono<ResponseEntity<ApiResponse<NewsArticleResponse>>> = mono {
        val result = getNewsUseCase.getNewsById(newsId)
        if (result != null) {
            ApiResponse.success(NewsArticleResponse.from(result)).toResponseEntity()
        } else {
            ApiResponse.error<NewsArticleResponse>("뉴스를 찾을 수 없습니다: $newsId").toResponseEntity()
        }
    }

    @GetMapping("/stock/{stockId}")
    @Operation(summary = "종목별 뉴스 조회")
    fun getNewsByStockId(
        @PathVariable stockId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<ApiResponse<NewsListResponse>>> = mono {
        val result = getNewsUseCase.getNewsByStockId(stockId, page, size)
        ApiResponse.success(NewsListResponse.from(result)).toResponseEntity()
    }
}
