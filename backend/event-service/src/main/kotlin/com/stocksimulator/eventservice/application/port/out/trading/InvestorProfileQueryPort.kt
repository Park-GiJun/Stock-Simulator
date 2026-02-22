package com.stocksimulator.eventservice.application.port.out.trading

interface InvestorProfileQueryPort {
    fun getNpcsByFrequency(frequency: String, maxCount: Int): List<NpcProfileDto>
    fun getInstitutionsByFrequency(frequency: String, maxCount: Int): List<InstitutionProfileDto>
    fun getAllNpcs(): List<NpcProfileDto>
    fun getAllInstitutions(): List<InstitutionProfileDto>
}

data class NpcProfileDto(
    val npcId: Long,
    val npcName: String,
    val investmentStyle: String,
    val capital: Long,
    val riskTolerance: Double,
    val preferredSectors: List<String>,
    val tradingFrequency: String
)

data class InstitutionProfileDto(
    val institutionId: Long,
    val institutionName: String,
    val institutionType: String,
    val investmentStyle: String,
    val capital: Long,
    val riskTolerance: Double,
    val preferredSectors: List<String>,
    val tradingFrequency: String
)
