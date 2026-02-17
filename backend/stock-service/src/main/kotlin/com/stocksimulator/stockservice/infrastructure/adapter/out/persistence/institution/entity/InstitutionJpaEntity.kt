package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.entity

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.stockservice.domain.model.InstitutionModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "institutions",
    schema = "stocks",
    indexes = [
        Index(name = "idx_institutions_type", columnList = "institution_type"),
        Index(name = "idx_institutions_name", columnList = "institution_name")
    ]
)
class InstitutionJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institution_id")
    val institutionId: Long? = null,

    @Column(name = "institution_name", nullable = false, unique = true, length = 100)
    val institutionName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "institution_type", nullable = false, length = 30)
    val institutionType: InstitutionType,

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_style", nullable = false, length = 20)
    val investmentStyle: InvestmentStyle,

    @Column(nullable = false)
    val capital: Long,

    @Column(name = "daily_income", nullable = false)
    val dailyIncome: Long,

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
    fun toDomain(): InstitutionModel = InstitutionModel(
        institutionId = institutionId,
        institutionName = institutionName,
        institutionType = institutionType,
        investmentStyle = investmentStyle,
        capital = capital,
        dailyIncome = dailyIncome,
        riskTolerance = riskTolerance,
        preferredSectors = preferredSectors.split(",").map { Sector.valueOf(it.trim()) },
        tradingFrequency = tradingFrequency,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InstitutionJpaEntity) return false
        return institutionId != null && institutionId == other.institutionId
    }

    override fun hashCode(): Int = institutionId?.hashCode() ?: 0

    companion object {
        fun fromDomain(domain: InstitutionModel): InstitutionJpaEntity = InstitutionJpaEntity(
            institutionId = domain.institutionId,
            institutionName = domain.institutionName,
            institutionType = domain.institutionType,
            investmentStyle = domain.investmentStyle,
            capital = domain.capital,
            dailyIncome = domain.dailyIncome,
            riskTolerance = domain.riskTolerance,
            preferredSectors = domain.preferredSectors.joinToString(",") { it.name },
            tradingFrequency = domain.tradingFrequency,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
