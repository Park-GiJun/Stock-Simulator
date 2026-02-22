package com.stocksimulator.stockservice.domain.model

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import java.time.Instant

data class InstitutionModel(
    val institutionId: Long?,
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
            institutionId = null,
            institutionName = institutionName,
            institutionType = institutionType,
            investmentStyle = investmentStyle,
            capital = capital,
            dailyIncome = capital / 100,
            riskTolerance = riskTolerance,
            preferredSectors = preferredSectors,
            tradingFrequency = tradingFrequency
        )
    }
}
