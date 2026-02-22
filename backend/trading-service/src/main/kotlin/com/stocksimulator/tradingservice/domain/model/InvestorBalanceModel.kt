package com.stocksimulator.tradingservice.domain.model

import com.stocksimulator.common.dto.TradingInvestorType
import java.time.Instant

data class InvestorBalanceModel(
    val id: Long? = null,
    val investorId: String,
    val investorType: TradingInvestorType,
    val cash: Long,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun deductCash(amount: Long): InvestorBalanceModel {
        require(amount > 0) { "Deduct amount must be positive" }
        require(cash >= amount) { "Insufficient balance: current=$cash, required=$amount" }
        return copy(cash = cash - amount, updatedAt = Instant.now())
    }

    fun addCash(amount: Long): InvestorBalanceModel {
        require(amount > 0) { "Add amount must be positive" }
        return copy(cash = cash + amount, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            investorId: String,
            investorType: TradingInvestorType,
            initialCash: Long
        ): InvestorBalanceModel = InvestorBalanceModel(
            investorId = investorId,
            investorType = investorType,
            cash = initialCash
        )
    }
}
