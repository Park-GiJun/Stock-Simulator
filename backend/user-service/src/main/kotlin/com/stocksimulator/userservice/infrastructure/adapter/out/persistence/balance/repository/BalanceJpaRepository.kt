package com.stocksimulator.userservice.infrastructure.adapter.out.persistence.balance.repository

import com.stocksimulator.userservice.infrastructure.adapter.out.persistence.balance.entity.BalanceJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceJpaRepository : JpaRepository<BalanceJpaEntity, Long> {
    fun findByUserId(userId: Long): BalanceJpaEntity?
}