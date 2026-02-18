package com.stocksimulator.stockservice.application.port.out.stock

import com.stocksimulator.stockservice.domain.model.StockModel

interface StockEventPublishPort {
    fun publishPriceUpdated(stock: StockModel, previousPrice: Long, volume: Long)
}
