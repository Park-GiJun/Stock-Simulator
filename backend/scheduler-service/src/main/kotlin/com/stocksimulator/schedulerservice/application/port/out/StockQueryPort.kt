package com.stocksimulator.schedulerservice.application.port.out

import com.stocksimulator.schedulerservice.domain.RandomStockInfo

interface StockQueryPort {
    fun getRandomListedStock(): RandomStockInfo?
}

