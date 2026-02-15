package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.stock.adapter

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.stock.entity.StockJpaEntity
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.stock.repository.StockJpaRepository
import com.stocksimulator.stockservice.application.port.out.stock.StockPersistencePort
import com.stocksimulator.stockservice.domain.StockModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class StockPersistenceAdapter(
    private val stockJpaRepository: StockJpaRepository
) : StockPersistencePort {

    override fun findById(stockId: String): StockModel? {
        return stockJpaRepository.findByStockId(stockId)?.toDomain()
    }

    override fun save(stock: StockModel): StockModel {
        return stockJpaRepository.save(StockJpaEntity.fromDomain(stock)).toDomain()
    }

    override fun update(stock: StockModel): StockModel {
        val entity = stockJpaRepository.findByStockId(stock.stockId)
            ?: return save(stock)
        entity.updateFromDomain(stock)
        return stockJpaRepository.save(entity).toDomain()
    }

    override fun findByFilters(
        status: StockStatus,
        sector: Sector?,
        marketCapGrade: MarketCapGrade?,
        pageable: Pageable
    ): Page<StockModel> {
        return stockJpaRepository.findByFilters(status, sector, marketCapGrade, pageable)
            .map { it.toDomain() }
    }

    override fun findByFiltersAndSearch(
        status: StockStatus,
        sector: Sector?,
        marketCapGrade: MarketCapGrade?,
        search: String,
        pageable: Pageable
    ): Page<StockModel> {
        return stockJpaRepository.findByFiltersAndSearch(status, sector, marketCapGrade, search, pageable)
            .map { it.toDomain() }
    }

    override fun searchByQuery(
        status: StockStatus,
        query: String,
        pageable: Pageable
    ): Page<StockModel> {
        return stockJpaRepository.searchByQuery(status, query, pageable)
            .map { it.toDomain() }
    }

    override fun existsByName(stockName: String): Boolean {
        return stockJpaRepository.existsByStockName(stockName)
    }

    override fun findRandomListed(): StockModel? {
        val page = stockJpaRepository.findRandomByStatus(StockStatus.LISTED, PageRequest.of(0, 1))
        return page.content.firstOrNull()?.toDomain()
    }
}
