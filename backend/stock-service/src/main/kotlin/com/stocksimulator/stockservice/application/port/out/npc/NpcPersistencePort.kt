package com.stocksimulator.stockservice.application.port.out.npc

import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.stockservice.domain.model.NpcModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface NpcPersistencePort {
    fun save(model: NpcModel): NpcModel
    fun findByName(name: String): NpcModel?
    fun existsByName(name: String): Boolean
    fun findAll(pageable: Pageable): Page<NpcModel>
    fun findAllNames(): List<String>
    fun findByTradingFrequency(frequency: TradingFrequency, pageable: Pageable): Page<NpcModel>
}
