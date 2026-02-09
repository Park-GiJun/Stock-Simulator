package com.stocksimulator.stockservice.application.port.out.stock

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.stockservice.domain.StockModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StockPersistencePort {
    fun findById(stockId: String): StockModel?
    fun save(stock: StockModel): StockModel
    fun update(stock: StockModel): StockModel
    fun findByFilters(
        status: StockStatus,
        sector: Sector?,
        marketCapGrade: MarketCapGrade?,
        pageable: Pageable
    ): Page<StockModel>
    fun findByFiltersAndSearch(
        status: StockStatus,
        sector: Sector?,
        marketCapGrade: MarketCapGrade?,
        search: String,
        pageable: Pageable
    ): Page<StockModel>
    fun searchByQuery(
        status: StockStatus,
        query: String,
        pageable: Pageable
    ): Page<StockModel>
}
