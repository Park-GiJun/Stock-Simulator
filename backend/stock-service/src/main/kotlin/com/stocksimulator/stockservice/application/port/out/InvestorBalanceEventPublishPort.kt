package com.stocksimulator.stockservice.application.port.out

interface InvestorBalanceEventPublishPort {
    fun publishBalanceInit(investorId: String, investorType: String, initialCash: Long)
}
