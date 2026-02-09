package com.stocksimulator.stockservice.application.handler.stock

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.stockservice.application.dto.query.stock.StockListQuery
import com.stocksimulator.stockservice.application.dto.result.stock.StockDetailResult
import com.stocksimulator.stockservice.application.dto.result.stock.StockListItemResult
import com.stocksimulator.stockservice.application.port.`in`.stock.GetStockDetailUseCase
import com.stocksimulator.stockservice.application.port.`in`.stock.GetStockListUseCase
import com.stocksimulator.stockservice.application.port.`in`.stock.SearchStockUseCase
import com.stocksimulator.stockservice.application.port.out.stock.StockPersistencePort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class StockQueryHandler(
    private val stockPersistencePort: StockPersistencePort
) : GetStockListUseCase, GetStockDetailUseCase, SearchStockUseCase {

    override fun getStockList(query: StockListQuery): Page<StockListItemResult> {
        val sortField = mapSortField(query.sortBy)
        val direction = if (query.sortOrder.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(query.page, query.size, Sort.by(direction, sortField))

        val sector = query.sector?.let { runCatching { Sector.valueOf(it) }.getOrNull() }
        val marketCap = query.marketCap?.let { runCatching { MarketCapGrade.valueOf(it) }.getOrNull() }

        val stocks = if (!query.search.isNullOrBlank()) {
            stockPersistencePort.findByFiltersAndSearch(
                status = StockStatus.LISTED,
                sector = sector,
                marketCapGrade = marketCap,
                search = query.search,
                pageable = pageable
            )
        } else {
            stockPersistencePort.findByFilters(
                status = StockStatus.LISTED,
                sector = sector,
                marketCapGrade = marketCap,
                pageable = pageable
            )
        }

        return stocks.map { StockListItemResult.from(it) }
    }

    override fun getStockDetail(stockId: String): StockDetailResult {
        val stock = stockPersistencePort.findById(stockId)
            ?: throw ResourceNotFoundException(ErrorCode.STOCK_NOT_FOUND)
        return StockDetailResult.from(stock)
    }

    override fun searchStocks(query: String): List<StockListItemResult> {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "stockName"))
        val stocks = stockPersistencePort.searchByQuery(
            status = StockStatus.LISTED,
            query = query,
            pageable = pageable
        )
        return stocks.content.map { StockListItemResult.from(it) }
    }

    private fun mapSortField(sortBy: String): String = when (sortBy) {
        "name", "stockName" -> "stockName"
        "price", "currentPrice" -> "currentPrice"
        "change" -> "currentPrice"
        "volume" -> "volume"
        "marketCap", "marketCapGrade" -> "marketCapGrade"
        else -> "stockName"
    }
}
