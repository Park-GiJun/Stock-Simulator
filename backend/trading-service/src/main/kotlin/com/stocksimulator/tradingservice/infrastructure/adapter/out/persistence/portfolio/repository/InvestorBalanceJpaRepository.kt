package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.repository

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity.InvestorBalanceJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InvestorBalanceJpaRepository : JpaRepository<InvestorBalanceJpaEntity, Long> {
    fun findByInvestorIdAndInvestorType(
        investorId: String,
        investorType: TradingInvestorType
    ): InvestorBalanceJpaEntity?
}
