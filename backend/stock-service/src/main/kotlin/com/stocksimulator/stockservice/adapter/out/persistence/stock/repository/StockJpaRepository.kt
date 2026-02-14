package com.stocksimulator.stockservice.adapter.out.persistence.stock.repository

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.stockservice.adapter.out.persistence.stock.entity.StockJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StockJpaRepository : JpaRepository<StockJpaEntity, String> {

    fun findByStockId(stockId: String): StockJpaEntity?

    fun findByStatus(status: StockStatus, pageable: Pageable): Page<StockJpaEntity>

    @Query("""
        SELECT s FROM StockJpaEntity s
        WHERE s.status = :status
        AND (:sector IS NULL OR s.sector = :sector)
        AND (:marketCapGrade IS NULL OR s.marketCapGrade = :marketCapGrade)
    """)
    fun findByFilters(
        status: StockStatus,
        sector: Sector?,
        marketCapGrade: MarketCapGrade?,
        pageable: Pageable
    ): Page<StockJpaEntity>

    @Query("""
        SELECT s FROM StockJpaEntity s
        WHERE s.status = :status
        AND (LOWER(s.stockName) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(s.stockId) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    fun searchByQuery(
        status: StockStatus,
        query: String,
        pageable: Pageable
    ): Page<StockJpaEntity>

    @Query("""
        SELECT s FROM StockJpaEntity s
        WHERE s.status = :status
        AND (:sector IS NULL OR s.sector = :sector)
        AND (:marketCapGrade IS NULL OR s.marketCapGrade = :marketCapGrade)
        AND (LOWER(s.stockName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(s.stockId) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun findByFiltersAndSearch(
        status: StockStatus,
        sector: Sector?,
        marketCapGrade: MarketCapGrade?,
        search: String,
        pageable: Pageable
    ): Page<StockJpaEntity>

    fun existsByStockName(stockName: String): Boolean
}
