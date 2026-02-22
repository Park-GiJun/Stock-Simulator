package com.stocksimulator.eventservice.application.port.out.trading

interface InvestorPortfolioQueryPort {
    fun getBalance(investorId: String, investorType: String): Long
    fun getPortfolioHoldings(investorId: String, investorType: String): List<PortfolioHoldingDto>
}

data class PortfolioHoldingDto(
    val stockId: String,
    val quantity: Long,
    val averagePrice: Long
)
