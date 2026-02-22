package com.stocksimulator.tradingservice.application.handler.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderStatus
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.application.port.out.order.OrderBookCachePort
import com.stocksimulator.tradingservice.application.port.out.order.OrderPersistencePort
import com.stocksimulator.tradingservice.domain.service.OrderBookDomainService
import com.stocksimulator.tradingservice.domain.vo.MatchResultVo
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshotVo
import com.stocksimulator.tradingservice.domain.vo.OrderEntryVo
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class OrderBookRegistry(
    private val orderBookCachePort: OrderBookCachePort,
    private val orderPersistencePort: OrderPersistencePort,
    private val redissonClient: RedissonClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val orderBooks = ConcurrentHashMap<String, OrderBookDomainService>()

    fun placeOrder(stockId: String, entry: OrderEntryVo, orderType: OrderType, orderKind: OrderKind): List<MatchResultVo> {
        return withStockLock(stockId) {
            getOrCreate(stockId).addOrder(entry, orderType, orderKind)
        }
    }

    fun cancelOrder(stockId: String, orderId: String): Boolean {
        return withStockLock(stockId) {
            getOrCreate(stockId).cancelOrder(orderId)
        }
    }

    fun getSnapshot(stockId: String, depth: Int = 10): OrderBookSnapshotVo {
        return withStockLock(stockId) {
            getOrCreate(stockId).getSnapshot(depth)
        }
    }

    fun persistToCache(stockId: String) {
        withStockLock(stockId) {
            val orderBook = orderBooks[stockId] ?: return@withStockLock
            val (bidEntries, askEntries) = orderBook.getAllEntries()
            orderBookCachePort.saveEntries(stockId, bidEntries.map { it.first }, OrderType.BUY)
            orderBookCachePort.saveEntries(stockId, askEntries.map { it.first }, OrderType.SELL)
            orderBookCachePort.saveSnapshot(stockId, orderBook.getSnapshot())
        }
    }

    fun persistToCacheAndDb(stockId: String) {
        withStockLock(stockId) {
            val orderBook = orderBooks[stockId] ?: return@withStockLock
            val (bidEntries, askEntries) = orderBook.getAllEntries()

            // Redis 캐시 저장
            orderBookCachePort.saveEntries(stockId, bidEntries.map { it.first }, OrderType.BUY)
            orderBookCachePort.saveEntries(stockId, askEntries.map { it.first }, OrderType.SELL)
            orderBookCachePort.saveSnapshot(stockId, orderBook.getSnapshot())

            // DB 잔량 동기화 (PENDING/PARTIALLY_FILLED 주문의 잔량 업데이트)
            val allEntries = bidEntries.map { it.first } + askEntries.map { it.first }
            if (allEntries.isNotEmpty()) {
                val remainingMap = allEntries.associate { it.orderId to it.remainingQuantity }
                orderPersistencePort.updateRemainingQuantities(remainingMap)
                log.debug("호가창 DB 동기화 완료: stockId={}, 주문수={}", stockId, allEntries.size)
            }
        }
    }

    fun seedIpoOrders(stockId: String, entries: List<OrderEntryVo>) {
        withStockLock(stockId) {
            getOrCreate(stockId).restore(entries, OrderType.SELL)
        }
    }

    private fun getOrCreate(stockId: String): OrderBookDomainService {
        return orderBooks.getOrPut(stockId) {
            val orderBook = OrderBookDomainService(stockId)
            var restored = false

            // 1차: Redis 캐시에서 복구
            try {
                val bidEntries = orderBookCachePort.loadEntries(stockId, OrderType.BUY)
                val askEntries = orderBookCachePort.loadEntries(stockId, OrderType.SELL)
                if (bidEntries.isNotEmpty()) orderBook.restore(bidEntries, OrderType.BUY)
                if (askEntries.isNotEmpty()) orderBook.restore(askEntries, OrderType.SELL)
                if (bidEntries.isNotEmpty() || askEntries.isNotEmpty()) {
                    log.info("호가창 Redis 복구 완료: stockId={}, 매수={}, 매도={}", stockId, bidEntries.size, askEntries.size)
                    restored = true
                }
            } catch (e: Exception) {
                log.warn("호가창 캐시 복구 실패: stockId={}, 원인={}", stockId, e.message)
            }

            // 2차: Redis 비어있으면 DB에서 PENDING 주문으로 복구
            if (!restored) {
                try {
                    val activeStatuses = listOf(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED)
                    val pendingOrders = orderPersistencePort.findByStockIdAndStatusIn(stockId, activeStatuses)
                    if (pendingOrders.isNotEmpty()) {
                        val buyEntries = pendingOrders
                            .filter { it.orderType == OrderType.BUY && it.price != null }
                            .map { OrderEntryVo(it.orderId, it.userId, it.price!!, it.remainingQuantity, it.createdAt) }
                        val sellEntries = pendingOrders
                            .filter { it.orderType == OrderType.SELL && it.price != null }
                            .map { OrderEntryVo(it.orderId, it.userId, it.price!!, it.remainingQuantity, it.createdAt) }

                        if (buyEntries.isNotEmpty()) orderBook.restore(buyEntries, OrderType.BUY)
                        if (sellEntries.isNotEmpty()) orderBook.restore(sellEntries, OrderType.SELL)
                        log.info("호가창 DB 복구 완료: stockId={}, 매수={}, 매도={}", stockId, buyEntries.size, sellEntries.size)

                        // 복구된 데이터를 Redis에 다시 캐싱
                        val (bidAll, askAll) = orderBook.getAllEntries()
                        orderBookCachePort.saveEntries(stockId, bidAll.map { it.first }, OrderType.BUY)
                        orderBookCachePort.saveEntries(stockId, askAll.map { it.first }, OrderType.SELL)
                        orderBookCachePort.saveSnapshot(stockId, orderBook.getSnapshot())
                    }
                } catch (e: Exception) {
                    log.warn("호가창 DB 복구 실패: stockId={}, 원인={}", stockId, e.message)
                }
            }

            orderBook
        }
    }

    private fun <T> withStockLock(stockId: String, action: () -> T): T {
        val lock = redissonClient.getLock("lock:orderbook:$stockId")
        val acquired = lock.tryLock(10, 30, TimeUnit.SECONDS)
        if (!acquired) {
            throw IllegalStateException("호가창 락 획득 실패: stockId=$stockId")
        }
        try {
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}
