package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.common.event.InvestorBalanceInitEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.ScheduleTradeEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.handler.portfolio.PortfolioCommandHandler
import com.stocksimulator.tradingservice.application.port.`in`.order.PlaceOrderUseCase
import com.stocksimulator.tradingservice.application.port.`in`.order.SeedIpoOrderBookUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class TradingEventConsumer(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val seedIpoOrderBookUseCase: SeedIpoOrderBookUseCase,
    private val portfolioCommandHandler: PortfolioCommandHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.SCHEDULE_TRADE], groupId = "trading-service")
    fun handleScheduleTrade(event: ScheduleTradeEvent, ack: Acknowledgment) {
        try {
            log.info(
                "스케줄 거래 이벤트 수신: investorId={}, stockId={}, orderType={}, quantity={}",
                event.investorId, event.stockId, event.orderType, event.quantity
            )

            val command = PlaceOrderCommand(
                userId = event.investorId,
                stockId = event.stockId,
                orderType = event.orderType,
                orderKind = OrderKind.MARKET,
                price = null,
                quantity = event.quantity,
                investorType = TradingInvestorType.valueOf(event.investorType)
            )
            placeOrderUseCase.placeOrder(command)

            log.info("NPC 거래 실행 완료: investorId={}, stockId={}", event.investorId, event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("스케줄 거래 이벤트 처리 실패: investorId={}, stockId={}", event.investorId, event.stockId, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.STOCK_LISTED], groupId = "trading-service")
    fun handleStockListed(event: StockListedEvent, ack: Acknowledgment) {
        try {
            log.info(
                "신규 상장 이벤트 수신: stockId={}, stockName={}, sector={}, basePrice={}, totalShares={}",
                event.stockId, event.stockName, event.sector, event.basePrice, event.totalShares
            )

            seedIpoOrderBookUseCase.distributeIpoShares(event)

            log.info("IPO 배분 처리 완료: stockId={}, stockName={}", event.stockId, event.stockName)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("신규 상장 이벤트 처리 실패: stockId={}, stockName={}", event.stockId, event.stockName, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.INVESTOR_BALANCE_INIT], groupId = "trading-service")
    fun handleBalanceInit(event: InvestorBalanceInitEvent, ack: Acknowledgment) {
        try {
            log.info(
                "잔고 초기화 이벤트 수신: investorId={}, type={}, cash={}",
                event.investorId, event.investorType, event.initialCash
            )

            val investorType = TradingInvestorType.valueOf(event.investorType)
            portfolioCommandHandler.initializeBalance(
                investorId = event.investorId,
                investorType = investorType,
                initialCash = event.initialCash
            )

            log.info("잔고 초기화 완료: investorId={}", event.investorId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("잔고 초기화 이벤트 처리 실패: investorId={}", event.investorId, e)
            ack.acknowledge()
        }
    }
}
