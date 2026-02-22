package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.trading.InvestorPortfolioQueryPort
import com.stocksimulator.eventservice.application.port.out.trading.PortfolioHoldingDto
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.TradingServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InvestorPortfolioQueryAdapter(
    private val tradingServiceFeignClient: TradingServiceFeignClient
) : InvestorPortfolioQueryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getBalance(investorId: String, investorType: String): Long {
        return try {
            val response = tradingServiceFeignClient.getBalance(investorId, investorType)
            response.data?.cash ?: 0L
        } catch (e: Exception) {
            log.warn("잔고 조회 실패: investorId={}, error={}", investorId, e.message)
            0L
        }
    }

    override fun getPortfolioHoldings(investorId: String, investorType: String): List<PortfolioHoldingDto> {
        return try {
            val response = tradingServiceFeignClient.getPortfolio(investorId, investorType)
            response.data?.holdings?.map { holding ->
                PortfolioHoldingDto(
                    stockId = holding.stockId,
                    quantity = holding.quantity,
                    averagePrice = holding.averagePrice
                )
            } ?: emptyList()
        } catch (e: Exception) {
            log.warn("포트폴리오 조회 실패: investorId={}, error={}", investorId, e.message)
            emptyList()
        }
    }
}
