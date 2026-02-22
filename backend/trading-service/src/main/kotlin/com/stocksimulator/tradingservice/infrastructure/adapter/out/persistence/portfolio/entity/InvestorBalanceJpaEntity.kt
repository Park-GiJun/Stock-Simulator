package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.portfolio.entity

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "investor_balances",
    schema = "trading",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_investor_balance", columnNames = ["investor_id", "investor_type"])
    ],
    indexes = [
        Index(name = "idx_investor_balances_investor", columnList = "investor_id, investor_type")
    ]
)
class InvestorBalanceJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "investor_id", nullable = false, length = 36)
    val investorId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "investor_type", nullable = false, length = 20)
    val investorType: TradingInvestorType,

    @Column(name = "cash", nullable = false)
    var cash: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): InvestorBalanceModel = InvestorBalanceModel(
        id = id,
        investorId = investorId,
        investorType = investorType,
        cash = cash,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFromDomain(domain: InvestorBalanceModel) {
        cash = domain.cash
        updatedAt = Instant.now()
    }

    companion object {
        fun fromDomain(domain: InvestorBalanceModel): InvestorBalanceJpaEntity = InvestorBalanceJpaEntity(
            id = domain.id,
            investorId = domain.investorId,
            investorType = domain.investorType,
            cash = domain.cash,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
