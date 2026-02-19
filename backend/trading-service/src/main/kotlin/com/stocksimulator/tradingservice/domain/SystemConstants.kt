package com.stocksimulator.tradingservice.domain

import com.stocksimulator.common.dto.Sector

object SystemConstants {
    const val SYSTEM_IPO_USER_ID = "SYSTEM_IPO"

    // 시스템 기관 투자자 (5개)
    val SYSTEM_INSTITUTIONS = (1..5).map { "SYSTEM_INST_%02d".format(it) }

    // 시스템 NPC 투자자 (10개)
    val SYSTEM_NPCS = (1..10).map { "SYSTEM_NPC_%02d".format(it) }

    // NPC별 관심 섹터 매핑
    val NPC_SECTOR_PREFERENCES: Map<String, List<Sector>> = mapOf(
        "SYSTEM_NPC_01" to listOf(Sector.IT, Sector.MANUFACTURING),
        "SYSTEM_NPC_02" to listOf(Sector.AGRICULTURE, Sector.FOOD),
        "SYSTEM_NPC_03" to listOf(Sector.SERVICE, Sector.REAL_ESTATE),
        "SYSTEM_NPC_04" to listOf(Sector.LUXURY, Sector.FOOD),
        "SYSTEM_NPC_05" to listOf(Sector.IT, Sector.SERVICE),
        "SYSTEM_NPC_06" to listOf(Sector.MANUFACTURING, Sector.AGRICULTURE),
        "SYSTEM_NPC_07" to listOf(Sector.REAL_ESTATE, Sector.LUXURY),
        "SYSTEM_NPC_08" to listOf(Sector.IT, Sector.FOOD),
        "SYSTEM_NPC_09" to listOf(Sector.SERVICE, Sector.MANUFACTURING),
        "SYSTEM_NPC_10" to listOf(Sector.AGRICULTURE, Sector.REAL_ESTATE)
    )
}
