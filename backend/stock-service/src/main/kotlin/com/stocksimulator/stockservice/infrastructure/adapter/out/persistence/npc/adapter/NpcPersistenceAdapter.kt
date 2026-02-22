package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.adapter

import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.stockservice.application.port.out.npc.NpcPersistencePort
import com.stocksimulator.stockservice.domain.model.NpcModel
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.entity.NpcJpaEntity
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.repository.NpcJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class NpcPersistenceAdapter(
    private val npcJpaRepository: NpcJpaRepository
) : NpcPersistencePort {

    override fun save(model: NpcModel): NpcModel {
        return npcJpaRepository.save(NpcJpaEntity.fromDomain(model)).toDomain()
    }

    override fun findByName(name: String): NpcModel? {
        return npcJpaRepository.findByNpcName(name)?.toDomain()
    }

    override fun existsByName(name: String): Boolean {
        return npcJpaRepository.existsByNpcName(name)
    }

    override fun findAll(pageable: Pageable): Page<NpcModel> {
        return npcJpaRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun findAllNames(): List<String> {
        return npcJpaRepository.findAllNpcNames()
    }

    override fun findByTradingFrequency(frequency: TradingFrequency, pageable: Pageable): Page<NpcModel> {
        return npcJpaRepository.findByTradingFrequency(frequency, pageable).map { it.toDomain() }
    }
}
