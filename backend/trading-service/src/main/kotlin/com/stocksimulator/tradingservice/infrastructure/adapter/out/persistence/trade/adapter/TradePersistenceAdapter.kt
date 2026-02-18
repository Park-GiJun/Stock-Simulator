package com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.trade.adapter

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.port.out.trade.TradePersistencePort
import com.stocksimulator.tradingservice.domain.model.TradeModel
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.trade.entity.TradeJpaEntity
import com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence.trade.repository.TradeJpaRepository
import org.springframework.stereotype.Component

@Component
class TradePersistenceAdapter(
    private val tradeJpaRepository: TradeJpaRepository
) : TradePersistencePort {

    override fun save(trade: TradeModel): TradeModel {
        return tradeJpaRepository.save(TradeJpaEntity.fromDomain(trade)).toDomain()
    }

    override fun findByInvestor(investorId: String, investorType: TradingInvestorType): List<TradeModel> {
        return tradeJpaRepository.findByInvestor(investorId, investorType).map { it.toDomain() }
    }

    override fun findByStockId(stockId: String): List<TradeModel> {
        return tradeJpaRepository.findByStockIdOrderByTradedAtDesc(stockId).map { it.toDomain() }
    }
}
