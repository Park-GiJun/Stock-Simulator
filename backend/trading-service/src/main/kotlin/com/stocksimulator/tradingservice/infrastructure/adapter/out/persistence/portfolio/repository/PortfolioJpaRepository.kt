package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.repository

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity.PortfolioJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PortfolioJpaRepository : JpaRepository<PortfolioJpaEntity, Long> {
    fun findByInvestorIdAndInvestorTypeAndStockId(
        investorId: String,
        investorType: TradingInvestorType,
        stockId: String
    ): PortfolioJpaEntity?

    fun findByInvestorIdAndInvestorType(
        investorId: String,
        investorType: TradingInvestorType
    ): List<PortfolioJpaEntity>
}
