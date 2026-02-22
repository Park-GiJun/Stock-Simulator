package com.stocksimulator.tradingservice.application.port.out.order

import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshotVo
import com.stocksimulator.tradingservice.domain.vo.OrderEntryVo

interface OrderBookCachePort {
    fun saveSnapshot(stockId: String, snapshot: OrderBookSnapshotVo)
    fun loadEntries(stockId: String, side: OrderType): List<OrderEntryVo>
    fun saveEntries(stockId: String, entries: List<OrderEntryVo>, side: OrderType)
    fun deleteEntries(stockId: String, orderIds: List<String>)
}
