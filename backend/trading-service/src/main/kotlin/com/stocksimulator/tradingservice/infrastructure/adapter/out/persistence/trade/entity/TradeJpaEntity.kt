package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.trade.entity

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.TradeModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "trades",
    schema = "trading",
    indexes = [
        Index(name = "idx_trades_buyer", columnList = "buyer_id, buyer_type"),
        Index(name = "idx_trades_seller", columnList = "seller_id, seller_type"),
        Index(name = "idx_trades_stock", columnList = "stock_id"),
        Index(name = "idx_trades_traded_at", columnList = "traded_at")
    ]
)
class TradeJpaEntity(
    @Id
    @Column(name = "trade_id", length = 36)
    val tradeId: String,

    @Column(name = "buy_order_id", nullable = false, length = 36)
    val buyOrderId: String,

    @Column(name = "sell_order_id", nullable = false, length = 36)
    val sellOrderId: String,

    @Column(name = "buyer_id", nullable = false, length = 36)
    val buyerId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", nullable = false, length = 20)
    val buyerType: TradingInvestorType,

    @Column(name = "seller_id", nullable = false, length = 36)
    val sellerId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_type", nullable = false, length = 20)
    val sellerType: TradingInvestorType,

    @Column(name = "stock_id", nullable = false, length = 20)
    val stockId: String,

    @Column(name = "price", nullable = false)
    val price: Long,

    @Column(name = "quantity", nullable = false)
    val quantity: Long,

    @Column(name = "trade_amount", nullable = false)
    val tradeAmount: Long,

    @Column(name = "traded_at", nullable = false)
    val tradedAt: Instant = Instant.now()
) {
    fun toDomain(): TradeModel = TradeModel(
        tradeId = tradeId,
        buyOrderId = buyOrderId,
        sellOrderId = sellOrderId,
        buyerId = buyerId,
        buyerType = buyerType,
        sellerId = sellerId,
        sellerType = sellerType,
        stockId = stockId,
        price = price,
        quantity = quantity,
        tradeAmount = tradeAmount,
        tradedAt = tradedAt
    )

    companion object {
        fun fromDomain(domain: TradeModel): TradeJpaEntity = TradeJpaEntity(
            tradeId = domain.tradeId,
            buyOrderId = domain.buyOrderId,
            sellOrderId = domain.sellOrderId,
            buyerId = domain.buyerId,
            buyerType = domain.buyerType,
            sellerId = domain.sellerId,
            sellerType = domain.sellerType,
            stockId = domain.stockId,
            price = domain.price,
            quantity = domain.quantity,
            tradeAmount = domain.tradeAmount,
            tradedAt = domain.tradedAt
        )
    }
}
