package com.stocksimulator.tradingservice.application.dto.result.order

import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshot
import com.stocksimulator.tradingservice.domain.vo.PriceLevel

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
            fun from(level: PriceLevel): PriceLevelResult {
                return PriceLevelResult(
                    price = level.price,
                    quantity = level.quantity,
                    orderCount = level.orderCount
                )
            }
        }
    }

    companion object {
        fun from(snapshot: OrderBookSnapshot): OrderBookResult {
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
