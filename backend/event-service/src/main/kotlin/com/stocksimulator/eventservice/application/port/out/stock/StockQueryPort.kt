package com.stocksimulator.eventservice.application.port.out.stock

import com.stocksimulator.eventservice.domain.RandomStockInfo

interface StockQueryPort {
    fun getRandomListedStock(): RandomStockInfo?
}
