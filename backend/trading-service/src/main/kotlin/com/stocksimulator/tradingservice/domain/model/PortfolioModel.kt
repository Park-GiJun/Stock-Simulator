package com.stocksimulator.tradingservice.domain.model

import com.stocksimulator.common.dto.TradingInvestorType
import java.time.Instant

data class PortfolioModel(
    val id: Long? = null,
    val investorId: String,
    val investorType: TradingInvestorType,
    val stockId: String,
    val quantity: Long,
    val averagePrice: Long,
    val totalInvested: Long,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun addHolding(buyQuantity: Long, buyPrice: Long): PortfolioModel {
        val newTotalInvested = totalInvested + (buyQuantity * buyPrice)
        val newQuantity = quantity + buyQuantity
        val newAveragePrice = if (newQuantity > 0) newTotalInvested / newQuantity else 0L
        return copy(
            quantity = newQuantity,
            averagePrice = newAveragePrice,
            totalInvested = newTotalInvested,
            updatedAt = Instant.now()
        )
    }

    fun removeHolding(sellQuantity: Long): PortfolioModel {
        require(sellQuantity <= quantity) { "Cannot sell more than held quantity" }
        val newQuantity = quantity - sellQuantity
        val newTotalInvested = if (newQuantity > 0) averagePrice * newQuantity else 0L
        return copy(
            quantity = newQuantity,
            totalInvested = newTotalInvested,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            investorId: String,
            investorType: TradingInvestorType,
            stockId: String
        ): PortfolioModel = PortfolioModel(
            investorId = investorId,
            investorType = investorType,
            stockId = stockId,
            quantity = 0,
            averagePrice = 0,
            totalInvested = 0
        )
    }
}
