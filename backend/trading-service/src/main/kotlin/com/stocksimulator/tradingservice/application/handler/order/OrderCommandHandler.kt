package com.stocksimulator.tradingservice.application.handler.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.exception.BusinessException
import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.InvalidInputException
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.common.util.PriceUtil
import com.stocksimulator.tradingservice.application.dto.command.order.CancelOrderCommand
import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.dto.result.order.PlaceOrderResult
import com.stocksimulator.tradingservice.application.port.`in`.order.CancelOrderUseCase
import com.stocksimulator.tradingservice.application.port.`in`.order.PlaceOrderUseCase
import com.stocksimulator.tradingservice.application.port.out.order.OrderPersistencePort
import com.stocksimulator.tradingservice.application.port.out.order.TradingEventPort
import com.stocksimulator.tradingservice.domain.model.OrderModel
import com.stocksimulator.tradingservice.domain.vo.MatchResult
import com.stocksimulator.tradingservice.domain.vo.OrderEntry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCommandHandler(
    private val orderPersistencePort: OrderPersistencePort,
    private val orderBookRegistry: OrderBookRegistry,
    private val tradingEventPort: TradingEventPort
) : PlaceOrderUseCase, CancelOrderUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun placeOrder(command: PlaceOrderCommand): PlaceOrderResult {
        // 1. 호가단위 검증 (지정가인 경우)
        if (command.orderKind == OrderKind.LIMIT) {
            requireNotNull(command.price) { "LIMIT order must have a price" }
            if (!PriceUtil.isValidPrice(command.price)) {
                throw InvalidInputException(
                    ErrorCode.INVALID_ORDER_PRICE,
                    "유효하지 않은 호가 단위입니다. 가격: ${command.price}, 호가단위: ${PriceUtil.getTickSize(command.price)}"
                )
            }
        }

        // 2. 도메인 모델 생성 및 DB 저장
        val order = OrderModel.create(
            userId = command.userId,
            stockId = command.stockId,
            orderType = command.orderType,
            orderKind = command.orderKind,
            price = command.price,
            quantity = command.quantity
        )
        orderPersistencePort.save(order)

        // 3. 호가창 매칭
        val entry = OrderEntry(
            orderId = order.orderId,
            userId = order.userId,
            price = command.price ?: 0L,
            remainingQuantity = order.quantity,
            timestamp = order.createdAt
        )
        val matches = orderBookRegistry.placeOrder(command.stockId, entry, command.orderType, command.orderKind)

        // 4. 매칭 결과 반영
        var updatedOrder = order
        val totalFilledQuantity = matches.sumOf { it.quantity }

        if (totalFilledQuantity > 0) {
            updatedOrder = updatedOrder.fill(totalFilledQuantity)
            orderPersistencePort.update(updatedOrder)
            updateMatchedOrders(matches)
        }

        // 5. 시장가 유동성 없으면 REJECTED
        if (command.orderKind == OrderKind.MARKET && totalFilledQuantity == 0L) {
            updatedOrder = updatedOrder.reject()
            orderPersistencePort.update(updatedOrder)
            log.info("시장가 주문 거부 (유동성 부족): orderId={}", order.orderId)
        }

        // 6. 캐시 저장 및 이벤트 발행
        orderBookRegistry.persistToCache(command.stockId)
        matches.forEach { tradingEventPort.publishOrderMatched(it) }
        tradingEventPort.publishOrderBookUpdated(orderBookRegistry.getSnapshot(command.stockId))

        log.info("주문 접수 완료: orderId={}, 상태={}, 체결건수={}", order.orderId, updatedOrder.status, matches.size)
        return PlaceOrderResult.from(updatedOrder, matches)
    }

    @Transactional
    override fun cancelOrder(command: CancelOrderCommand) {
        val order = orderPersistencePort.findById(command.orderId)
            ?: throw ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND)

        if (order.userId != command.userId) {
            throw BusinessException(ErrorCode.FORBIDDEN, "본인의 주문만 취소할 수 있습니다")
        }

        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PARTIALLY_FILLED) {
            throw BusinessException(
                ErrorCode.ORDER_ALREADY_CANCELLED,
                "취소할 수 없는 주문 상태입니다: ${order.status.displayName}"
            )
        }

        orderBookRegistry.cancelOrder(order.stockId, order.orderId)

        val cancelledOrder = order.cancel()
        orderPersistencePort.update(cancelledOrder)

        orderBookRegistry.persistToCache(order.stockId)
        tradingEventPort.publishOrderCancelled(order.orderId, order.userId, order.stockId, "사용자 취소")
        tradingEventPort.publishOrderBookUpdated(orderBookRegistry.getSnapshot(order.stockId))

        log.info("주문 취소 완료: orderId={}", order.orderId)
    }

    private fun updateMatchedOrders(matches: List<MatchResult>) {
        val matchQuantityByOrderId = mutableMapOf<String, Long>()
        for (match in matches) {
            matchQuantityByOrderId.merge(match.buyOrderId, match.quantity) { a, b -> a + b }
            matchQuantityByOrderId.merge(match.sellOrderId, match.quantity) { a, b -> a + b }
        }
        for ((orderId, matchedQty) in matchQuantityByOrderId) {
            val existing = orderPersistencePort.findById(orderId) ?: continue
            if (existing.status == OrderStatus.PENDING || existing.status == OrderStatus.PARTIALLY_FILLED) {
                orderPersistencePort.update(existing.fill(matchedQty))
            }
        }
    }
}
