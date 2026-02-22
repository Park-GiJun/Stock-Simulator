package com.stocksimulator.tradingservice.application.port.`in`.portfolio

import com.stocksimulator.tradingservice.application.dto.query.portfolio.TradeHistoryQuery
import com.stocksimulator.tradingservice.application.dto.result.portfolio.TradeResult

interface GetTradeHistoryUseCase {
    fun getTradeHistory(query: TradeHistoryQuery): List<TradeResult>
    fun getTradesByStock(stockId: String): List<TradeResult>
}
