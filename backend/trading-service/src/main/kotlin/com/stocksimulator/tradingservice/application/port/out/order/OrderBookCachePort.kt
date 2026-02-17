package com.stocksimulator.tradingservice.application.port.out.order

import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshot
import com.stocksimulator.tradingservice.domain.vo.OrderEntry

interface OrderBookCachePort {
    fun saveSnapshot(stockId: String, snapshot: OrderBookSnapshot)
    fun loadEntries(stockId: String, side: OrderType): List<OrderEntry>
    fun saveEntries(stockId: String, entries: List<OrderEntry>, side: OrderType)
    fun deleteEntries(stockId: String, orderIds: List<String>)
}
