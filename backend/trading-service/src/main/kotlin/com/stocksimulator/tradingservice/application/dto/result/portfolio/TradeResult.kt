package com.stocksimulator.tradingservice.application.dto.result.portfolio

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.TradeModel

data class TradeResult(
    val tradeId: String,
    val buyOrderId: String,
    val sellOrderId: String,
    val buyerId: String,
    val buyerType: TradingInvestorType,
    val sellerId: String,
    val sellerType: TradingInvestorType,
    val stockId: String,
    val price: Long,
    val quantity: Long,
    val tradeAmount: Long,
    val tradedAt: String
) {
    companion object {
        fun from(model: TradeModel): TradeResult = TradeResult(
            tradeId = model.tradeId,
            buyOrderId = model.buyOrderId,
            sellOrderId = model.sellOrderId,
            buyerId = model.buyerId,
            buyerType = model.buyerType,
            sellerId = model.sellerId,
            sellerType = model.sellerType,
            stockId = model.stockId,
            price = model.price,
            quantity = model.quantity,
            tradeAmount = model.tradeAmount,
            tradedAt = model.tradedAt.toString()
        )
    }
}
