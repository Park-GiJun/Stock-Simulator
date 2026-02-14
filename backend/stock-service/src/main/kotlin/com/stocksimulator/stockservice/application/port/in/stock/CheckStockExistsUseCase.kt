package com.stocksimulator.stockservice.application.port.`in`.stock

interface CheckStockExistsUseCase {
    fun existsByStockId(stockId: String): Boolean
    fun existsByStockName(stockName: String): Boolean
}
