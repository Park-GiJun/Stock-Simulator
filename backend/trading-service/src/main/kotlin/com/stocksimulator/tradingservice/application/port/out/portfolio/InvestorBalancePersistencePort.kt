package com.stocksimulator.tradingservice.application.port.out.portfolio

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel

interface InvestorBalancePersistencePort {
    fun findByInvestor(investorId: String, investorType: TradingInvestorType): InvestorBalanceModel?
    fun save(balance: InvestorBalanceModel): InvestorBalanceModel
}
