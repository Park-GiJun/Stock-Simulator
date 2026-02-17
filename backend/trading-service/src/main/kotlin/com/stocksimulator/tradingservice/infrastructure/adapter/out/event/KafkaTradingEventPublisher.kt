package com.stocksimulator.tradingservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.OrderBookEntry
import com.stocksimulator.common.event.OrderBookUpdatedEvent
import com.stocksimulator.common.event.OrderCancelledEvent
import com.stocksimulator.common.event.OrderMatchedEvent
import com.stocksimulator.tradingservice.application.port.out.order.TradingEventPort
import com.stocksimulator.tradingservice.domain.vo.MatchResult
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshot
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaTradingEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : TradingEventPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishOrderMatched(matchResult: MatchResult) {
        val event = OrderMatchedEvent(
            tradeId = matchResult.tradeId,
            buyOrderId = matchResult.buyOrderId,
            sellOrderId = matchResult.sellOrderId,
            buyUserId = matchResult.buyUserId,
            sellUserId = matchResult.sellUserId,
            stockId = matchResult.stockId,
            price = matchResult.price,
            quantity = matchResult.quantity,
            matchedAt = matchResult.matchedAt
        )
        kafkaTemplate.send(KafkaTopics.ORDER_MATCHED, matchResult.stockId, event)
        log.debug("주문 체결 이벤트 발행: tradeId={}, stockId={}", matchResult.tradeId, matchResult.stockId)
    }

    override fun publishOrderCancelled(orderId: String, userId: String, stockId: String, reason: String) {
        val event = OrderCancelledEvent(
            orderId = orderId,
            userId = userId,
            stockId = stockId,
            reason = reason
        )
        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, stockId, event)
        log.debug("주문 취소 이벤트 발행: orderId={}, stockId={}", orderId, stockId)
    }

    override fun publishOrderBookUpdated(snapshot: OrderBookSnapshot) {
        val event = OrderBookUpdatedEvent(
            stockId = snapshot.stockId,
            bidOrders = snapshot.bids.map { OrderBookEntry(it.price, it.quantity, it.orderCount) },
            askOrders = snapshot.asks.map { OrderBookEntry(it.price, it.quantity, it.orderCount) }
        )
        kafkaTemplate.send(KafkaTopics.ORDERBOOK_UPDATED, snapshot.stockId, event)
        log.debug("호가창 업데이트 이벤트 발행: stockId={}", snapshot.stockId)
    }
}
