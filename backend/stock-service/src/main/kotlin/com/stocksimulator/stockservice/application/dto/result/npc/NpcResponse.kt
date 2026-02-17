package com.stocksimulator.stockservice.application.dto.result.npc

import com.stocksimulator.stockservice.domain.model.NpcModel
import java.time.Instant

data class NpcResponse(
    val npcId: Long?,
    val npcName: String,
    val investmentStyle: String,
    val capital: Long,
    val weeklyIncome: Long,
    val riskTolerance: Double,
    val preferredSectors: List<String>,
    val tradingFrequency: String,
    val createdAt: Instant
) {
    companion object {
        fun from(model: NpcModel): NpcResponse = NpcResponse(
            npcId = model.npcId,
            npcName = model.npcName,
            investmentStyle = model.investmentStyle.name,
            capital = model.capital,
            weeklyIncome = model.weeklyIncome,
            riskTolerance = model.riskTolerance,
            preferredSectors = model.preferredSectors.map { it.name },
            tradingFrequency = model.tradingFrequency.name,
            createdAt = model.createdAt
        )
    }
}
