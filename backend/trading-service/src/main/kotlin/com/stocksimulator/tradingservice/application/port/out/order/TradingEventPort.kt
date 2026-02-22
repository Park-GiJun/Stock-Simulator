package com.stocksimulator.tradingservice.application.port.out.order

import com.stocksimulator.tradingservice.domain.vo.MatchResultVo
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshotVo

interface TradingEventPort {
    fun publishOrderMatched(matchResult: MatchResultVo)
    fun publishOrderCancelled(orderId: String, userId: String, stockId: String, reason: String)
    fun publishOrderBookUpdated(snapshot: OrderBookSnapshotVo)
}
