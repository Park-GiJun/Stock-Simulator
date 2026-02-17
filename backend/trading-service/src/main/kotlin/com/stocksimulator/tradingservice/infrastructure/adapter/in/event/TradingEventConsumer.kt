package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.ScheduleTradeEvent
import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.port.`in`.order.PlaceOrderUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class TradingEventConsumer(
    private val placeOrderUseCase: PlaceOrderUseCase
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
                quantity = event.quantity
            )
            placeOrderUseCase.placeOrder(command)

            log.info("NPC 거래 실행 완료: investorId={}, stockId={}", event.investorId, event.stockId)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("스케줄 거래 이벤트 처리 실패: investorId={}, stockId={}", event.investorId, event.stockId, e)
            ack.acknowledge()
        }
    }
}
