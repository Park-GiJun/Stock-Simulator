package com.stocksimulator.tradingservice.application.handler.portfolio

import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.tradingservice.application.dto.query.portfolio.InvestorBalanceQuery
import com.stocksimulator.tradingservice.application.dto.query.portfolio.PortfolioQuery
import com.stocksimulator.tradingservice.application.dto.query.portfolio.TradeHistoryQuery
import com.stocksimulator.tradingservice.application.dto.result.portfolio.InvestorBalanceResult
import com.stocksimulator.tradingservice.application.dto.result.portfolio.PortfolioResult
import com.stocksimulator.tradingservice.application.dto.result.portfolio.TradeResult
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetInvestorBalanceUseCase
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetPortfolioUseCase
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetTradeHistoryUseCase
import com.stocksimulator.tradingservice.application.port.out.portfolio.InvestorBalancePersistencePort
import com.stocksimulator.tradingservice.application.port.out.portfolio.PortfolioPersistencePort
import com.stocksimulator.tradingservice.application.port.out.trade.TradePersistencePort
import org.springframework.stereotype.Service

@Service
class PortfolioQueryHandler(
    private val portfolioPersistencePort: PortfolioPersistencePort,
    private val investorBalancePersistencePort: InvestorBalancePersistencePort,
    private val tradePersistencePort: TradePersistencePort
) : GetPortfolioUseCase, GetTradeHistoryUseCase, GetInvestorBalanceUseCase {

    override fun getPortfolio(query: PortfolioQuery): List<PortfolioResult> {
        return portfolioPersistencePort.findByInvestor(query.investorId, query.investorType)
            .filter { it.quantity > 0 }
            .map { PortfolioResult.from(it) }
    }

    override fun getTradeHistory(query: TradeHistoryQuery): List<TradeResult> {
        return tradePersistencePort.findByInvestor(query.investorId, query.investorType)
            .map { TradeResult.from(it) }
    }

    override fun getTradesByStock(stockId: String): List<TradeResult> {
        return tradePersistencePort.findByStockId(stockId)
            .map { TradeResult.from(it) }
    }

    override fun getBalance(query: InvestorBalanceQuery): InvestorBalanceResult {
        val balance = investorBalancePersistencePort.findByInvestor(query.investorId, query.investorType)
            ?: throw ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "잔고 정보가 없습니다")
        return InvestorBalanceResult.from(balance)
    }
}
