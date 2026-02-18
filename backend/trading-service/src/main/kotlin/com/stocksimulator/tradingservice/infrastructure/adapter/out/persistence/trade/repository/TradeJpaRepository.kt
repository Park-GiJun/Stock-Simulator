package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.trade.repository

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.trade.entity.TradeJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TradeJpaRepository : JpaRepository<TradeJpaEntity, String> {
    @Query("""
        SELECT t FROM TradeJpaEntity t
        WHERE (t.buyerId = :investorId AND t.buyerType = :investorType)
           OR (t.sellerId = :investorId AND t.sellerType = :investorType)
        ORDER BY t.tradedAt DESC
    """)
    fun findByInvestor(investorId: String, investorType: TradingInvestorType): List<TradeJpaEntity>

    fun findByStockIdOrderByTradedAtDesc(stockId: String): List<TradeJpaEntity>
}
