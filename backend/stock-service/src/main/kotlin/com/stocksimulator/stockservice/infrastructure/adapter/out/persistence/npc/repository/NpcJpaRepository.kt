package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.repository

import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.npc.entity.NpcJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NpcJpaRepository : JpaRepository<NpcJpaEntity, Long> {

    fun findByNpcName(name: String): NpcJpaEntity?

    fun existsByNpcName(name: String): Boolean
}
