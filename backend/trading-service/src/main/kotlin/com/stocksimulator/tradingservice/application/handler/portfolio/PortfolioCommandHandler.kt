package com.stocksimulator.tradingservice.application.handler.portfolio

import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.tradingservice.application.port.out.portfolio.InvestorBalancePersistencePort
import com.stocksimulator.tradingservice.application.port.out.portfolio.PortfolioPersistencePort
import com.stocksimulator.tradingservice.application.port.out.trade.TradePersistencePort
import com.stocksimulator.tradingservice.domain.model.InvestorBalanceModel
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import com.stocksimulator.tradingservice.domain.model.TradeModel
import com.stocksimulator.tradingservice.domain.vo.MatchResultVo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PortfolioCommandHandler(
    private val portfolioPersistencePort: PortfolioPersistencePort,
    private val investorBalancePersistencePort: InvestorBalancePersistencePort,
    private val tradePersistencePort: TradePersistencePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun settleMatches(
        matches: List<MatchResultVo>,
        buyerType: TradingInvestorType,
        sellerTypes: Map<String, TradingInvestorType>
    ) {
        for (match in matches) {
            val sellerType = sellerTypes[match.sellUserId] ?: TradingInvestorType.USER
            val tradeAmount = match.price * match.quantity

            // 1. 체결 이력 저장
            val trade = TradeModel(
                tradeId = match.tradeId,
                buyOrderId = match.buyOrderId,
                sellOrderId = match.sellOrderId,
                buyerId = match.buyUserId,
                buyerType = buyerType,
                sellerId = match.sellUserId,
                sellerType = sellerType,
                stockId = match.stockId,
                price = match.price,
                quantity = match.quantity,
                tradeAmount = tradeAmount,
                tradedAt = match.matchedAt
            )
            tradePersistencePort.save(trade)

            // 2. 매수자: 포트폴리오 추가, 잔고 차감
            settleBuyer(match.buyUserId, buyerType, match.stockId, match.quantity, match.price, tradeAmount)

            // 3. 매도자: 포트폴리오 감소, 잔고 증가
            settleSeller(match.sellUserId, sellerType, match.stockId, match.quantity, tradeAmount)
        }
    }

    fun initializeBalance(investorId: String, investorType: TradingInvestorType, initialCash: Long) {
        val existing = investorBalancePersistencePort.findByInvestor(investorId, investorType)
        if (existing == null) {
            val balance = InvestorBalanceModel.create(investorId, investorType, initialCash)
            investorBalancePersistencePort.save(balance)
            log.info("잔고 초기화: investorId={}, type={}, cash={}", investorId, investorType, initialCash)
        }
    }

    fun ensureBalance(investorId: String, investorType: TradingInvestorType, capital: Long) {
        val existing = investorBalancePersistencePort.findByInvestor(investorId, investorType)
        if (existing == null) {
            val balance = InvestorBalanceModel.create(investorId, investorType, capital)
            investorBalancePersistencePort.save(balance)
            log.info("잔고 Lazy Init: investorId={}, type={}, cash={}", investorId, investorType, capital)
        }
    }

    private fun settleBuyer(
        buyerId: String,
        buyerType: TradingInvestorType,
        stockId: String,
        quantity: Long,
        price: Long,
        tradeAmount: Long
    ) {
        // 포트폴리오 추가/증가
        val portfolio = portfolioPersistencePort.findByInvestorAndStock(buyerId, buyerType, stockId)
            ?: PortfolioModel.create(buyerId, buyerType, stockId)
        val updatedPortfolio = portfolio.addHolding(quantity, price)
        portfolioPersistencePort.save(updatedPortfolio)

        // 잔고 차감
        val balance = investorBalancePersistencePort.findByInvestor(buyerId, buyerType)
        if (balance != null && balance.cash >= tradeAmount) {
            investorBalancePersistencePort.save(balance.deductCash(tradeAmount))
        } else {
            log.warn("잔고 부족 - 매수 정산 스킵: buyerId={}, required={}, available={}",
                buyerId, tradeAmount, balance?.cash ?: 0)
        }
    }

    private fun settleSeller(
        sellerId: String,
        sellerType: TradingInvestorType,
        stockId: String,
        quantity: Long,
        tradeAmount: Long
    ) {
        // 포트폴리오 감소
        val portfolio = portfolioPersistencePort.findByInvestorAndStock(sellerId, sellerType, stockId)
        if (portfolio != null && portfolio.quantity >= quantity) {
            val updatedPortfolio = portfolio.removeHolding(quantity)
            portfolioPersistencePort.save(updatedPortfolio)
        } else {
            log.warn("보유수량 부족 - 매도 포트폴리오 정산 스킵: sellerId={}, required={}, available={}",
                sellerId, quantity, portfolio?.quantity ?: 0)
        }

        // 잔고 증가
        val balance = investorBalancePersistencePort.findByInvestor(sellerId, sellerType)
        if (balance != null) {
            investorBalancePersistencePort.save(balance.addCash(tradeAmount))
        } else {
            log.warn("잔고 없음 - 매도 잔고 정산 스킵: sellerId={}", sellerId)
        }
    }
}
