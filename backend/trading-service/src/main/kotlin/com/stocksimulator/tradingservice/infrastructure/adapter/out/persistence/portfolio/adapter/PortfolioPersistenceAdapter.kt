package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.adapter

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.port.out.portfolio.PortfolioPersistencePort
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity.PortfolioJpaEntity
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.repository.PortfolioJpaRepository
import org.springframework.stereotype.Component

@Component
class PortfolioPersistenceAdapter(
    private val portfolioJpaRepository: PortfolioJpaRepository
) : PortfolioPersistencePort {

    override fun findByInvestorAndStock(
        investorId: String,
        investorType: TradingInvestorType,
        stockId: String
    ): PortfolioModel? {
        return portfolioJpaRepository.findByInvestorIdAndInvestorTypeAndStockId(investorId, investorType, stockId)
            ?.toDomain()
    }

    override fun findByInvestor(investorId: String, investorType: TradingInvestorType): List<PortfolioModel> {
        return portfolioJpaRepository.findByInvestorIdAndInvestorType(investorId, investorType)
            .map { it.toDomain() }
    }

    override fun save(portfolio: PortfolioModel): PortfolioModel {
        val existing = portfolio.id?.let { portfolioJpaRepository.findById(it).orElse(null) }
        return if (existing != null) {
            existing.updateFromDomain(portfolio)
            portfolioJpaRepository.save(existing).toDomain()
        } else {
            portfolioJpaRepository.save(PortfolioJpaEntity.fromDomain(portfolio)).toDomain()
        }
    }
}
