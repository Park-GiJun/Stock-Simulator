package com.stocksimulator.tradingservice.domain.model

import com.stocksimulator.common.dto.TradingInvestorType
import java.time.Instant
import java.util.UUID

data class TradeModel(
    val tradeId: String = UUID.randomUUID().toString(),
    val buyOrderId: String,
    val sellOrderId: String,
    val buyerId: String,
    val buyerType: TradingInvestorType,
    val sellerId: String,
    val sellerType: TradingInvestorType,
    val stockId: String,
    val price: Long,
    val quantity: Long,
    val tradeAmount: Long = price * quantity,
    val tradedAt: Instant = Instant.now()
)
