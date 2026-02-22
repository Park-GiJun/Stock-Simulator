package com.stocksimulator.tradingservice.domain.model

import java.time.Instant

data class PortfolioModel(
    val id: Long? = null,
    val investorId: String,
    val investorType: String,
    val stockId: String,
    val quantity: Long,
    val averagePrice: Long,
    val totalInvested: Long,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
