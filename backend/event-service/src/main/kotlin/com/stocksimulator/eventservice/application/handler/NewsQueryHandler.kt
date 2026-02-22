package com.stocksimulator.eventservice.application.handler

import com.stocksimulator.eventservice.application.dto.query.NewsListQuery
import com.stocksimulator.eventservice.application.dto.result.NewsArticleResult
import com.stocksimulator.eventservice.application.dto.result.NewsListResult
import com.stocksimulator.eventservice.application.port.`in`.GetNewsUseCase
import com.stocksimulator.eventservice.application.port.out.NewsPersistencePort
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class NewsQueryHandler(
    private val newsPersistencePort: NewsPersistencePort
) : GetNewsUseCase {

    override suspend fun getNewsById(newsId: String): NewsArticleResult? {
        return newsPersistencePort.findById(newsId)?.let { NewsArticleResult.from(it) }
    }

    override suspend fun getNewsList(query: NewsListQuery): NewsListResult {
        val pageable = PageRequest.of(query.page, query.size, Sort.by(Sort.Direction.DESC, "publishedAt"))

        val page = when {
            query.stockId != null -> newsPersistencePort.findByStockId(query.stockId, pageable)
            query.sector != null -> newsPersistencePort.findBySector(query.sector, pageable)
            else -> newsPersistencePort.findAll(pageable)
        }

        return NewsListResult(
            news = page.content.map { NewsArticleResult.from(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages
        )
    }

    override suspend fun getNewsByStockId(stockId: String, page: Int, size: Int): NewsListResult {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        val result = newsPersistencePort.findByStockId(stockId, pageable)

        return NewsListResult(
            news = result.content.map { NewsArticleResult.from(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }
}
