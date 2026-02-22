package com.stocksimulator.userservice.infrastructure.adapter.out.persistence.user.adapter

import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.userservice.infrastructure.adapter.out.persistence.user.entity.UserJpaEntity
import com.stocksimulator.userservice.infrastructure.adapter.out.persistence.user.repository.UserJpaRepository
import com.stocksimulator.userservice.application.port.out.user.UserPersistencePort
import com.stocksimulator.userservice.domain.model.UserModel
import org.springframework.stereotype.Component

@Component
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserPersistencePort {

    override fun findByEmail(email: String): UserModel? {
        return userJpaRepository.findByEmail(email)?.toDomain()
    }

    override fun findByUsername(username: String): UserModel? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    override fun findById(userId: Long): UserModel? {
        return userJpaRepository.findByUserId(userId)?.toDomain()
    }

    override fun save(user: UserModel): UserModel {
        return userJpaRepository.save(UserJpaEntity.fromDomain(user, user.password)).toDomain()
    }

    override fun update(user: UserModel): UserModel {
        return userJpaRepository.save(UserJpaEntity.fromDomain(user, user.password)).toDomain()
    }
}