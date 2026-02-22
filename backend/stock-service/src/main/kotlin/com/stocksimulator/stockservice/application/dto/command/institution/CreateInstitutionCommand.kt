package com.stocksimulator.stockservice.application.dto.command.institution

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency

data class CreateInstitutionCommand(
    val institutionName: String,
    val institutionType: InstitutionType,
    val investmentStyle: InvestmentStyle,
    val capital: Long,
    val dailyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<Sector>,
    val tradingFrequency: TradingFrequency
)
