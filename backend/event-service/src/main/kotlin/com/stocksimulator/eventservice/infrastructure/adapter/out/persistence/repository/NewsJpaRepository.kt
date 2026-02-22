package com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.repository

import com.stocksimulator.eventservice.infrastructure.adapter.out.persistence.entity.NewsJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface NewsJpaRepository : JpaRepository<NewsJpaEntity, String>, JpaSpecificationExecutor<NewsJpaEntity> {
    fun findByStockId(stockId: String, pageable: Pageable): Page<NewsJpaEntity>
    fun findBySector(sector: String, pageable: Pageable): Page<NewsJpaEntity>
}
