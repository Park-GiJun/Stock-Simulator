package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.entity

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.stockservice.domain.model.NpcModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "npcs",
    schema = "stocks",
    indexes = [
        Index(name = "idx_npcs_name", columnList = "npc_name"),
        Index(name = "idx_npcs_investment_style", columnList = "investment_style")
    ]
)
class NpcJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "npc_id")
    val npcId: Long? = null,

    @Column(name = "npc_name", nullable = false, length = 100)
    val npcName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_style", nullable = false, length = 20)
    val investmentStyle: InvestmentStyle,

    @Column(nullable = false)
    val capital: Long,

    @Column(name = "weekly_income", nullable = false)
    val weeklyIncome: Long,

    @Column(name = "risk_tolerance", nullable = false)
    val riskTolerance: Double,

    @Column(name = "preferred_sectors", nullable = false, columnDefinition = "TEXT")
    val preferredSectors: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "trading_frequency", nullable = false, length = 10)
    val tradingFrequency: TradingFrequency,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): NpcModel = NpcModel(
        npcId = npcId,
        npcName = npcName,
        investmentStyle = investmentStyle,
        capital = capital,
        weeklyIncome = weeklyIncome,
        riskTolerance = riskTolerance,
        preferredSectors = preferredSectors.split(",").map { Sector.valueOf(it.trim()) },
        tradingFrequency = tradingFrequency,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NpcJpaEntity) return false
        return npcId != null && npcId == other.npcId
    }

    override fun hashCode(): Int = npcId?.hashCode() ?: 0

    companion object {
        fun fromDomain(domain: NpcModel): NpcJpaEntity = NpcJpaEntity(
            npcId = domain.npcId,
            npcName = domain.npcName,
            investmentStyle = domain.investmentStyle,
            capital = domain.capital,
            weeklyIncome = domain.weeklyIncome,
            riskTolerance = domain.riskTolerance,
            preferredSectors = domain.preferredSectors.joinToString(",") { it.name },
            tradingFrequency = domain.tradingFrequency,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
