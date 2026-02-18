package com.stocksimulator.stockservice.application.dto.result.institution

import com.stocksimulator.stockservice.domain.model.InstitutionModel
import java.time.Instant

data class InstitutionResult(
    val institutionId: Long?,
    val institutionName: String,
    val institutionType: String,
    val investmentStyle: String,
    val capital: Long,
    val dailyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<String>,
    val tradingFrequency: String,
    val createdAt: Instant
) {
    companion object {
        fun from(model: InstitutionModel): InstitutionResult = InstitutionResult(
            institutionId = model.institutionId,
            institutionName = model.institutionName,
            institutionType = model.institutionType.name,
            investmentStyle = model.investmentStyle.name,
            capital = model.capital,
            dailyIncome = model.dailyIncome,
            riskTolerance = model.riskTolerance,
            preferredSectors = model.preferredSectors.map { it.name },
            tradingFrequency = model.tradingFrequency.name,
            createdAt = model.createdAt
        )
    }
}
