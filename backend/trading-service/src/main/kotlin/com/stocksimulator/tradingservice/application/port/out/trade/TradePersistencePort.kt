package com.stocksimulator.tradingservice.application.port.out.trade

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.domain.model.TradeModel

interface TradePersistencePort {
    fun save(trade: TradeModel): TradeModel
    fun findByInvestor(investorId: String, investorType: TradingInvestorType): List<TradeModel>
    fun findByStockId(stockId: String): List<TradeModel>
}
