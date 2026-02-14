package com.stocksimulator.schedulerservice.application.port.out

interface StockExistenceCheckPort {
    fun existsByStockId(stockId: String): Boolean
    fun existsByStockName(stockName: String): Boolean
}
