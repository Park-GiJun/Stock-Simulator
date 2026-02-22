package com.stocksimulator.eventservice.application.port.out

import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface NewsPersistencePort {
    fun save(news: NewsArticleModel): NewsArticleModel
    fun findById(newsId: String): NewsArticleModel?
    fun findAll(pageable: Pageable): Page<NewsArticleModel>
    fun findByStockId(stockId: String, pageable: Pageable): Page<NewsArticleModel>
    fun findBySector(sector: String, pageable: Pageable): Page<NewsArticleModel>
    fun findByFilters(level: String?, sentiment: String?, sector: String?, stockId: String?, pageable: Pageable): Page<NewsArticleModel>
}
