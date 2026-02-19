package com.stocksimulator.tradingservice.application.port.`in`.order

import com.stocksimulator.common.event.StockListedEvent

interface SeedIpoOrderBookUseCase {
    fun distributeIpoShares(event: StockListedEvent)
}
