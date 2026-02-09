package com.stocksimulator.stockservice.application.port.`in`.stock

import com.stocksimulator.stockservice.application.dto.query.stock.StockListQuery
import com.stocksimulator.stockservice.application.dto.result.stock.StockListItemResult
import org.springframework.data.domain.Page

interface GetStockListUseCase {
    fun getStockList(query: StockListQuery): Page<StockListItemResult>
}
