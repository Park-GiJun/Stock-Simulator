package com.stocksimulator.stockservice.application.dto.command.npc

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency

data class CreateNpcCommand(
    val npcName: String,
    val investmentStyle: InvestmentStyle,
    val capital: Long,
    val weeklyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<Sector>,
    val tradingFrequency: TradingFrequency
)
