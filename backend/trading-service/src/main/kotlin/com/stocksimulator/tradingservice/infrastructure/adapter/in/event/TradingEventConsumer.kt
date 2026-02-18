package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.ScheduleTradeEvent
import com.stocksimulator.common.event.UserCreatedEvent
import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.handler.portfolio.PortfolioCommandHandler
import com.stocksimulator.tradingservice.application.port.`in`.order.PlaceOrderUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class TradingEventConsumer(
    private val placeOrderUseCase: PlaceOrderUseCase,
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

            val investorType = mapInvestorType(event.investorType)

            // Lazy init: 잔고 없으면 capital로 초기화
            if (event.capital > 0) {
                portfolioCommandHandler.ensureBalance(event.investorId, investorType, event.capital)
            }

            val command = PlaceOrderCommand(
                userId = event.investorId,
                stockId = event.stockId,
                orderType = event.orderType,
                orderKind = OrderKind.MARKET,
                price = null,
                quantity = event.quantity,
                investorType = investorType
            )
            placeOrderUseCase.placeOrder(command)

            log.info("NPC 거래 실행 완료: investorId={}, stockId={}", event.investorId, event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("스케줄 거래 이벤트 처리 실패: investorId={}, stockId={}", event.investorId, event.stockId, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(topics = [KafkaTopics.USER_CREATED], groupId = "trading-service")
    fun handleUserCreated(event: UserCreatedEvent, ack: Acknowledgment) {
        try {
            log.info("사용자 생성 이벤트 수신: userId={}, initialCapital={}", event.userId, event.initialCapital)
            portfolioCommandHandler.initializeBalance(
                investorId = event.userId,
                investorType = TradingInvestorType.USER,
                initialCash = event.initialCapital
            )
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("사용자 생성 이벤트 처리 실패: userId={}", event.userId, e)
            ack.acknowledge()
        }
    }

    private fun mapInvestorType(type: String): TradingInvestorType {
        return when (type.uppercase()) {
            "NPC", "INDIVIDUAL" -> TradingInvestorType.NPC
            "INSTITUTION" -> TradingInvestorType.INSTITUTION
            else -> TradingInvestorType.USER
        }
    }
}
