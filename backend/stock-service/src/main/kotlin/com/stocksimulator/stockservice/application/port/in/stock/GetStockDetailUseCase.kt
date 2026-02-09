package com.stocksimulator.stockservice.application.port.`in`.stock

import com.stocksimulator.stockservice.application.dto.result.stock.StockDetailResult

interface GetStockDetailUseCase {
    fun getStockDetail(stockId: String): StockDetailResult
}
