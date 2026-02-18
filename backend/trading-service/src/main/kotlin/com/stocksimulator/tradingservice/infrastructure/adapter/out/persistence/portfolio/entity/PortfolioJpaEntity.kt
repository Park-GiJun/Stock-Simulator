package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "portfolios",
    schema = "trading",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_portfolio_investor_stock", columnNames = ["investor_id", "investor_type", "stock_id"])
    ],
    indexes = [
        Index(name = "idx_portfolios_investor", columnList = "investor_id, investor_type"),
        Index(name = "idx_portfolios_stock", columnList = "stock_id")
    ]
)
class PortfolioJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "investor_id", nullable = false, length = 36)
    val investorId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "investor_type", nullable = false, length = 20)
    val investorType: TradingInvestorType,

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
        investorType = investorType,
        stockId = stockId,
        quantity = quantity,
        averagePrice = averagePrice,
        totalInvested = totalInvested,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFromDomain(domain: PortfolioModel) {
        quantity = domain.quantity
        averagePrice = domain.averagePrice
        totalInvested = domain.totalInvested
        updatedAt = Instant.now()
    }

    companion object {
        fun fromDomain(domain: PortfolioModel): PortfolioJpaEntity = PortfolioJpaEntity(
            id = domain.id,
            investorId = domain.investorId,
            investorType = domain.investorType,
            stockId = domain.stockId,
            quantity = domain.quantity,
            averagePrice = domain.averagePrice,
            totalInvested = domain.totalInvested,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
