package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.repository

import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.entity.NpcJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NpcJpaRepository : JpaRepository<NpcJpaEntity, Long> {

    fun findByNpcName(name: String): NpcJpaEntity?

    fun existsByNpcName(name: String): Boolean

    @Query("SELECT n.npcName FROM NpcJpaEntity n")
    fun findAllNpcNames(): List<String>
}
