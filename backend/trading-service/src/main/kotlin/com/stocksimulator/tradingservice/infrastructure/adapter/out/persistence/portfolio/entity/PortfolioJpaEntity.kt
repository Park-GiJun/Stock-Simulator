package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "portfolios",
    schema = "trading",
    indexes = [
        Index(name = "idx_portfolios_investor_id", columnList = "investor_id"),
        Index(name = "idx_portfolios_stock_id", columnList = "stock_id"),
        Index(name = "idx_portfolios_investor_type", columnList = "investor_type")
    ]
)
class PortfolioJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "investor_id", nullable = false, length = 36)
    val investorId: String,

    @Column(name = "investor_type", nullable = false, length = 20)
    val investorType: String,

    @Column(name = "stock_id", nullable = false, length = 20)
    val stockId: String,

    @Column(name = "quantity", nullable = false)
    var quantity: Long,

    @Column(name = "average_price", nullable = false)
    var averagePrice: Long,

    @Column(name = "total_invested", nullable = false)
    var totalInvested: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): PortfolioModel = PortfolioModel(
        id = id,
        investorId = investorId,
        investorType = TradingInvestorType.valueOf(investorType),
        stockId = stockId,
        quantity = quantity,
        averagePrice = averagePrice,
        totalInvested = totalInvested,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: PortfolioModel): PortfolioJpaEntity = PortfolioJpaEntity(
            id = domain.id,
            investorId = domain.investorId,
            investorType = domain.investorType.name,
            stockId = domain.stockId,
            quantity = domain.quantity,
            averagePrice = domain.averagePrice,
            totalInvested = domain.totalInvested,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
