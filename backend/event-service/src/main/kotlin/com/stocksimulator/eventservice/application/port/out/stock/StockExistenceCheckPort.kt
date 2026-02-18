package com.stocksimulator.eventservice.application.port.out.stock

interface StockExistenceCheckPort {
    fun existsByStockId(stockId: String): Boolean
    fun existsByStockName(stockName: String): Boolean
}
