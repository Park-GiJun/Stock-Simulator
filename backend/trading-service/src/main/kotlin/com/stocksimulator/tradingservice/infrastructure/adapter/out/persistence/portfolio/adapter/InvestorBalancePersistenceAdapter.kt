package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.adapter

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.port.out.portfolio.InvestorBalancePersistencePort
import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity.InvestorBalanceJpaEntity
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.repository.InvestorBalanceJpaRepository
import org.springframework.stereotype.Component

@Component
class InvestorBalancePersistenceAdapter(
    private val investorBalanceJpaRepository: InvestorBalanceJpaRepository
) : InvestorBalancePersistencePort {

    override fun findByInvestor(investorId: String, investorType: TradingInvestorType): InvestorBalanceModel? {
        return investorBalanceJpaRepository.findByInvestorIdAndInvestorType(investorId, investorType)
            ?.toDomain()
    }

    override fun save(balance: InvestorBalanceModel): InvestorBalanceModel {
        val existing = balance.id?.let { investorBalanceJpaRepository.findById(it).orElse(null) }
        return if (existing != null) {
            existing.updateFromDomain(balance)
            investorBalanceJpaRepository.save(existing).toDomain()
        } else {
            investorBalanceJpaRepository.save(InvestorBalanceJpaEntity.fromDomain(balance)).toDomain()
        }
    }
}
