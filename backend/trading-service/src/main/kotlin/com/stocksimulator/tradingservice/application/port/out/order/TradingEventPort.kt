package com.stocksimulator.tradingservice.application.port.out.order

import com.stocksimulator.tradingservice.domain.vo.MatchResult
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshot

interface TradingEventPort {
    fun publishOrderMatched(matchResult: MatchResult)
    fun publishOrderCancelled(orderId: String, userId: String, stockId: String, reason: String)
    fun publishOrderBookUpdated(snapshot: OrderBookSnapshot)
}
