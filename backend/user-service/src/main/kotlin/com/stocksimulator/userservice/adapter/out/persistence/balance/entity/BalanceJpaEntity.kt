package com.stocksimulator.userservice.adapter.out.persistence.balance.entity

import com.stocksimulator.userservice.domain.BalanceModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "balances",
    schema = "users",
    indexes = [
        Index(name = "idx_balances_user_id", columnList = "user_id")
    ]
)
class BalanceJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    val balanceId: Long? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(nullable = false)
    var cash: Long = 5_000_000L,

    @Column(name = "total_asset", nullable = false)
    var totalAsset: Long = 5_000_000L,

    @Column(name = "stock_value", nullable = false)
    var stockValue: Long = 0L,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): BalanceModel {
        return BalanceModel.of(
            balanceId = balanceId ?: throw IllegalStateException("Balance ID must not be null"),
            userId = userId,
            cash = cash,
            totalAsset = totalAsset,
            stockValue = stockValue,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun updateFromDomain(balance: BalanceModel) {
        this.cash = balance.cash
        this.totalAsset = balance.totalAsset
        this.stockValue = balance.stockValue
        this.updatedAt = balance.updatedAt
    }

    companion object {
        fun fromDomain(balance: BalanceModel): BalanceJpaEntity {
            return BalanceJpaEntity(
                balanceId = balance.balanceId,
                userId = balance.userId,
                cash = balance.cash,
                totalAsset = balance.totalAsset,
                stockValue = balance.stockValue,
                createdAt = balance.createdAt,
                updatedAt = balance.updatedAt
            )
        }
    }
}
