package com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.adapter

import com.stocksimulator.eventservice.application.port.out.NewsPersistencePort
import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.entity.NewsJpaEntity
import com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.repository.NewsJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class NewsPersistenceAdapter(
    private val newsJpaRepository: NewsJpaRepository
) : NewsPersistencePort {

    override fun save(news: NewsArticleModel): NewsArticleModel {
        return newsJpaRepository.save(NewsJpaEntity.fromDomain(news)).toDomain()
    }

    override fun findById(newsId: String): NewsArticleModel? {
        return newsJpaRepository.findById(newsId).orElse(null)?.toDomain()
    }

    override fun findAll(pageable: Pageable): Page<NewsArticleModel> {
        return newsJpaRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun findByStockId(stockId: String, pageable: Pageable): Page<NewsArticleModel> {
        return newsJpaRepository.findByStockId(stockId, pageable).map { it.toDomain() }
    }

    override fun findBySector(sector: String, pageable: Pageable): Page<NewsArticleModel> {
        return newsJpaRepository.findBySector(sector, pageable).map { it.toDomain() }
    }
}
