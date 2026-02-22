package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.repository

import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.entity.NpcJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NpcJpaRepository : JpaRepository<NpcJpaEntity, Long> {

    fun findByNpcName(name: String): NpcJpaEntity?

    fun existsByNpcName(name: String): Boolean

    @Query("SELECT n.npcName FROM NpcJpaEntity n")
    fun findAllNpcNames(): List<String>

    fun findByTradingFrequency(tradingFrequency: TradingFrequency, pageable: Pageable): Page<NpcJpaEntity>
}
