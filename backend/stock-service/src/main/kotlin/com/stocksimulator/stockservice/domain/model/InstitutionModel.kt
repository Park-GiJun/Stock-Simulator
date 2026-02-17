package com.stocksimulator.stockservice.domain.model

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import java.time.Instant

data class InstitutionModel(
    val institutionId: Long? = null,
    val institutionName: String,
    val institutionType: InstitutionType,
    val investmentStyle: InvestmentStyle,
    val capital: Long,
    val dailyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<Sector>,
    val tradingFrequency: TradingFrequency,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(institutionName.isNotBlank()) { "institutionName must not be blank" }
        require(capital > 0) { "capital must be positive" }
        require(dailyIncome >= 0) { "dailyIncome must not be negative" }
        require(riskTolerance in 0.0..1.0) { "riskTolerance must be between 0 and 1" }
        require(preferredSectors.isNotEmpty()) { "preferredSectors must not be empty" }
    }

    companion object {
        fun create(
            institutionName: String,
            institutionType: InstitutionType,
            investmentStyle: InvestmentStyle,
            capital: Long,
            riskTolerance: Double,
            preferredSectors: List<Sector>,
            tradingFrequency: TradingFrequency
        ): InstitutionModel = InstitutionModel(
            institutionName = institutionName,
            institutionType = institutionType,
            investmentStyle = investmentStyle,
            capital = capital,
            dailyIncome = capital / 100,  // 자본금의 1%
            riskTolerance = riskTolerance,
            preferredSectors = preferredSectors,
            tradingFrequency = tradingFrequency
        )
    }

    fun receiveDailyIncome(): InstitutionModel = copy(
        capital = capital + dailyIncome,
        updatedAt = Instant.now()
    )
}
