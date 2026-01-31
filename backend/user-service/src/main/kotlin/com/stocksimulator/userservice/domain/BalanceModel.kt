package com.stocksimulator.userservice.domain

import java.time.Instant

/**
 * Balance Domain Model
 * - ID는 Long? (IDENTITY 전략)
 */
data class BalanceModel(
    val balanceId: Long? = null,
    val userId: Long,
    val cash: Long = 5_000_000L,
    val totalAsset: Long = 5_000_000L,
    val stockValue: Long = 0L,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
){
    init {
        require(cash >= 0) { "현금은 0 이상이어야 합니다." }
        require(totalAsset >= 0) { "총자산은 0 이상이어야 합니다." }
        require(stockValue >= 0) { "주식 평가액은 0 이상이어야 합니다." }
    }

    companion object {
        /**
         * 신규 계좌 생성 (초기 자본금 500만원)
         */
        fun create(userId: Long): BalanceModel {
            return BalanceModel(
                balanceId = null,
                userId = userId,
                cash = 5_000_000L,
                totalAsset = 5_000_000L,
                stockValue = 0L,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }

        /**
         * 기존 계좌 재구성 (DB 조회)
         */
        fun of(
            balanceId: Long,
            userId: Long,
            cash: Long,
            totalAsset: Long,
            stockValue: Long,
            createdAt: Instant,
            updatedAt: Instant
        ): BalanceModel {
            return BalanceModel(
                balanceId = balanceId,
                userId = userId,
                cash = cash,
                totalAsset = totalAsset,
                stockValue = stockValue,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    /**
     * 현금 차감 (주식 매수)
     */
    fun deductCash(amount: Long): BalanceModel {
        require(amount > 0) { "차감 금액은 0보다 커야 합니다." }
        require(cash >= amount) { "잔액이 부족합니다. 현재: $cash, 필요: $amount" }

        return this.copy(
            cash = cash - amount,
            updatedAt = Instant.now()
        )
    }

    /**
     * 현금 증가 (주식 매도)
     */
    fun addCash(amount: Long): BalanceModel {
        require(amount > 0) { "증가 금액은 0보다 커야 합니다." }

        return this.copy(
            cash = cash + amount,
            updatedAt = Instant.now()
        )
    }

    /**
     * 총자산 재계산
     */
    fun recalculateTotalAsset(currentStockValue: Long): BalanceModel {
        return this.copy(
            stockValue = currentStockValue,
            totalAsset = cash + currentStockValue,
            updatedAt = Instant.now()
        )
    }
}
