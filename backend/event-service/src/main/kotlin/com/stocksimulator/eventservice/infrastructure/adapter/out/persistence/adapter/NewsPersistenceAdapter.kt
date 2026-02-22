package com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.adapter

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sentiment
import com.stocksimulator.eventservice.application.port.out.NewsPersistencePort
import com.stocksimulator.eventservice.domain.model.NewsArticleModel
import com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.entity.NewsJpaEntity
import com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.repository.NewsJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
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

    override fun findByFilters(
        level: String?,
        sentiment: String?,
        sector: String?,
        stockId: String?,
        pageable: Pageable
    ): Page<NewsArticleModel> {
        val spec = Specification<NewsJpaEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            level?.let {
                predicates.add(cb.equal(root.get<Any>("level"), EventLevel.valueOf(it)))
            }
            sentiment?.let {
                predicates.add(cb.equal(root.get<Any>("sentiment"), Sentiment.valueOf(it)))
            }
            sector?.let {
                predicates.add(cb.equal(root.get<Any>("sector"), it))
            }
            stockId?.let {
                predicates.add(cb.equal(root.get<Any>("stockId"), it))
            }

            if (predicates.isEmpty()) {
                cb.conjunction()
            } else {
                cb.and(*predicates.toTypedArray())
            }
        }

        return newsJpaRepository.findAll(spec, pageable).map { it.toDomain() }
    }
}
