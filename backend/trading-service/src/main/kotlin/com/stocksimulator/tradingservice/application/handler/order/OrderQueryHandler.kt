package com.stocksimulator.tradingservice.application.handler.order

import com.stocksimulator.tradingservice.application.dto.query.order.OrderBookQuery
import com.stocksimulator.tradingservice.application.dto.result.order.OrderBookResult
import com.stocksimulator.tradingservice.application.port.`in`.order.GetOrderBookUseCase
import org.springframework.stereotype.Service

@Service
class OrderQueryHandler(
    private val orderBookRegistry: OrderBookRegistry
) : GetOrderBookUseCase {

    override fun getOrderBook(query: OrderBookQuery): OrderBookResult {
        val snapshot = orderBookRegistry.getSnapshot(query.stockId, query.depth)
        return OrderBookResult.from(snapshot)
    }
}
