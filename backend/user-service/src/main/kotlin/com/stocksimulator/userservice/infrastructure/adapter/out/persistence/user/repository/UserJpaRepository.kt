package com.stocksimulator.userservice.infrastructure.adapter.out.persistence.user.repository

import com.stocksimulator.userservice.infrastructure.adapter.out.persistence.user.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByEmail(email: String): UserJpaEntity?
    fun findByUsername(username: String): UserJpaEntity?
    fun findByUserId(userId: Long): UserJpaEntity?
}