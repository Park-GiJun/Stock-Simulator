package com.stocksimulator.eventservice.application.port.`in`

import com.stocksimulator.eventservice.application.dto.query.NewsListQuery
import com.stocksimulator.eventservice.application.dto.result.NewsArticleResult
import com.stocksimulator.eventservice.application.dto.result.NewsListResult

interface GetNewsUseCase {
    suspend fun getNewsById(newsId: String): NewsArticleResult?
    suspend fun getNewsList(query: NewsListQuery): NewsListResult
    suspend fun getNewsByStockId(stockId: String, page: Int, size: Int): NewsListResult
}
