package com.stocksimulator.stockservice.domain.model

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import java.time.Instant

data class NpcModel(
    val npcId: Long? = null,
    val npcName: String,
    val investmentStyle: InvestmentStyle,
    val capital: Long,
    val weeklyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<Sector>,
    val tradingFrequency: TradingFrequency,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(npcName.isNotBlank()) { "npcName must not be blank" }
        require(capital > 0) { "capital must be positive" }
        require(weeklyIncome >= 0) { "weeklyIncome must not be negative" }
        require(riskTolerance in 0.0..1.0) { "riskTolerance must be between 0 and 1" }
        require(preferredSectors.isNotEmpty()) { "preferredSectors must not be empty" }
    }

    companion object {
        fun create(
            npcName: String,
            investmentStyle: InvestmentStyle,
            capital: Long,
            riskTolerance: Double,
            preferredSectors: List<Sector>,
            tradingFrequency: TradingFrequency
        ): NpcModel = NpcModel(
            npcName = npcName,
            investmentStyle = investmentStyle,
            capital = capital,
            weeklyIncome = capital * 5 / 100,
            riskTolerance = riskTolerance,
            preferredSectors = preferredSectors,
            tradingFrequency = tradingFrequency
        )
    }

    fun receiveWeeklyIncome(): NpcModel = copy(
        capital = capital + weeklyIncome,
        updatedAt = Instant.now()
    )
}
