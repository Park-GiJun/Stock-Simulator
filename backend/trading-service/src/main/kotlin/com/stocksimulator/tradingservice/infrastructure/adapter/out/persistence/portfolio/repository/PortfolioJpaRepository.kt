package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.repository

import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity.PortfolioJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PortfolioJpaRepository : JpaRepository<PortfolioJpaEntity, Long> {
    fun findByInvestorIdAndInvestorTypeAndStockId(
        investorId: String,
        investorType: String,
        stockId: String
    ): PortfolioJpaEntity?

    fun findByInvestorIdAndInvestorType(
        investorId: String,
        investorType: String
    ): List<PortfolioJpaEntity>

    fun findByStockId(stockId: String): List<PortfolioJpaEntity>
}
