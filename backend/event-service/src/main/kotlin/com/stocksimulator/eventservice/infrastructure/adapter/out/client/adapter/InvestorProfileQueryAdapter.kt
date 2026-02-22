package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.trading.InstitutionProfileDto
import com.stocksimulator.eventservice.application.port.out.trading.InvestorProfileQueryPort
import com.stocksimulator.eventservice.application.port.out.trading.NpcProfileDto
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.StockServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InvestorProfileQueryAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : InvestorProfileQueryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getNpcsByFrequency(frequency: String, maxCount: Int): List<NpcProfileDto> {
        return try {
            val response = stockServiceFeignClient.getNpcsByFrequency(frequency, maxCount)
            response.data?.map { npc ->
                NpcProfileDto(
                    npcId = npc.npcId ?: 0L,
                    npcName = npc.npcName,
                    investmentStyle = npc.investmentStyle,
                    capital = npc.capital,
                    riskTolerance = npc.riskTolerance,
                    preferredSectors = npc.preferredSectors,
                    tradingFrequency = npc.tradingFrequency
                )
            } ?: emptyList()
        } catch (e: Exception) {
            log.error("NPC 프로필 조회 실패: frequency={}, error={}", frequency, e.message)
            emptyList()
        }
    }

    override fun getInstitutionsByFrequency(frequency: String, maxCount: Int): List<InstitutionProfileDto> {
        return try {
            val response = stockServiceFeignClient.getInstitutionsByFrequency(frequency, maxCount)
            response.data?.map { inst ->
                InstitutionProfileDto(
                    institutionId = inst.institutionId ?: 0L,
                    institutionName = inst.institutionName,
                    institutionType = inst.institutionType,
                    investmentStyle = inst.investmentStyle,
                    capital = inst.capital,
                    riskTolerance = inst.riskTolerance,
                    preferredSectors = inst.preferredSectors,
                    tradingFrequency = inst.tradingFrequency
                )
            } ?: emptyList()
        } catch (e: Exception) {
            log.error("기관투자자 프로필 조회 실패: frequency={}, error={}", frequency, e.message)
            emptyList()
        }
    }

    override fun getAllNpcs(): List<NpcProfileDto> {
        return try {
            val response = stockServiceFeignClient.getAllNpcs()
            response.data?.map { npc ->
                NpcProfileDto(
                    npcId = npc.npcId ?: 0L,
                    npcName = npc.npcName,
                    investmentStyle = npc.investmentStyle,
                    capital = npc.capital,
                    riskTolerance = npc.riskTolerance,
                    preferredSectors = npc.preferredSectors,
                    tradingFrequency = npc.tradingFrequency
                )
            } ?: emptyList()
        } catch (e: Exception) {
            log.error("전체 NPC 프로필 조회 실패: error={}", e.message)
            emptyList()
        }
    }

    override fun getAllInstitutions(): List<InstitutionProfileDto> {
        return try {
            val response = stockServiceFeignClient.getAllInstitutions()
            response.data?.map { inst ->
                InstitutionProfileDto(
                    institutionId = inst.institutionId ?: 0L,
                    institutionName = inst.institutionName,
                    institutionType = inst.institutionType,
                    investmentStyle = inst.investmentStyle,
                    capital = inst.capital,
                    riskTolerance = inst.riskTolerance,
                    preferredSectors = inst.preferredSectors,
                    tradingFrequency = inst.tradingFrequency
                )
            } ?: emptyList()
        } catch (e: Exception) {
            log.error("전체 기관투자자 프로필 조회 실패: error={}", e.message)
            emptyList()
        }
    }
}
