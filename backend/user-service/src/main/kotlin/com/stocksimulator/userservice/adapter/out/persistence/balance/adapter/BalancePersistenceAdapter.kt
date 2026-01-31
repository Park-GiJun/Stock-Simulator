package com.stocksimulator.userservice.adapter.out.persistence.balance.adapter

import com.stocksimulator.userservice.adapter.out.persistence.balance.entity.BalanceJpaEntity
import com.stocksimulator.userservice.adapter.out.persistence.balance.repository.BalanceJpaRepository
import com.stocksimulator.userservice.application.port.out.balance.BalancePersistencePort
import com.stocksimulator.userservice.domain.BalanceModel
import org.springframework.stereotype.Component

@Component
class BalancePersistenceAdapter(
    private val balanceJpaRepository: BalanceJpaRepository
) : BalancePersistencePort {

    override fun save(balance: BalanceModel): BalanceModel {
        return balanceJpaRepository.save(BalanceJpaEntity.fromDomain(balance)).toDomain()
    }

    override fun findByUserId(userId: Long): BalanceModel? {
        return balanceJpaRepository.findByUserId(userId)?.toDomain()
    }

    override fun update(balance: BalanceModel): BalanceModel {
        return balanceJpaRepository.save(BalanceJpaEntity.fromDomain(balance)).toDomain()
    }
}