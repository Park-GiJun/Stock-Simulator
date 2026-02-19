package com.stocksimulator.tradingservice.application.dto.result.order

import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshotVo
import com.stocksimulator.tradingservice.domain.vo.PriceLevelVo

data class OrderBookResult(
    val stockId: String,
    val bids: List<PriceLevelResult>,
    val asks: List<PriceLevelResult>,
    val bestBid: Long?,
    val bestAsk: Long?,
    val spread: Long?
) {
    data class PriceLevelResult(
        val price: Long,
        val quantity: Long,
        val orderCount: Int
    ) {
        companion object {
            fun from(level: PriceLevelVo): PriceLevelResult {
                return PriceLevelResult(
                    price = level.price,
                    quantity = level.quantity,
                    orderCount = level.orderCount
                )
            }
        }
    }

    companion object {
        fun from(snapshot: OrderBookSnapshotVo): OrderBookResult {
            return OrderBookResult(
                stockId = snapshot.stockId,
                bids = snapshot.bids.map { PriceLevelResult.from(it) },
                asks = snapshot.asks.map { PriceLevelResult.from(it) },
                bestBid = snapshot.bestBid,
                bestAsk = snapshot.bestAsk,
                spread = snapshot.spread
            )
        }
    }
}
