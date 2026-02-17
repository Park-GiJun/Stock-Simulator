package com.stocksimulator.tradingservice.application.port.`in`.order

import com.stocksimulator.tradingservice.application.dto.query.order.OrderBookQuery
import com.stocksimulator.tradingservice.application.dto.result.order.OrderBookResult

interface GetOrderBookUseCase {
    fun getOrderBook(query: OrderBookQuery): OrderBookResult
}
