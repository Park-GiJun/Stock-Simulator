package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.adapter

import com.stocksimulator.tradingservice.application.port.out.portfolio.PortfolioPersistencePort
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity.PortfolioJpaEntity
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.repository.PortfolioJpaRepository
import org.springframework.stereotype.Component

@Component
class PortfolioPersistenceAdapter(
    private val portfolioJpaRepository: PortfolioJpaRepository
) : PortfolioPersistencePort {

    override fun save(portfolio: PortfolioModel): PortfolioModel {
        return portfolioJpaRepository.save(PortfolioJpaEntity.fromDomain(portfolio)).toDomain()
    }

    override fun saveAll(portfolios: List<PortfolioModel>): List<PortfolioModel> {
        val entities = portfolios.map { PortfolioJpaEntity.fromDomain(it) }
        return portfolioJpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByInvestorAndStock(
        investorId: String,
        investorType: String,
        stockId: String
    ): PortfolioModel? {
        return portfolioJpaRepository.findByInvestorIdAndInvestorTypeAndStockId(
            investorId, investorType, stockId
        )?.toDomain()
    }
}
