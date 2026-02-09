package com.stocksimulator.stockservice.application.port.`in`.stock

import com.stocksimulator.stockservice.application.dto.result.stock.StockListItemResult

interface SearchStockUseCase {
    fun searchStocks(query: String): List<StockListItemResult>
}
