package com.stocksimulator.tradingservice.application.handler.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.application.port.out.order.OrderBookCachePort
import com.stocksimulator.tradingservice.domain.service.OrderBook
import com.stocksimulator.tradingservice.domain.vo.MatchResult
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshot
import com.stocksimulator.tradingservice.domain.vo.OrderEntry
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class OrderBookRegistry(
    private val orderBookCachePort: OrderBookCachePort,
    private val redissonClient: RedissonClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val orderBooks = ConcurrentHashMap<String, OrderBook>()

    fun placeOrder(stockId: String, entry: OrderEntry, orderType: OrderType, orderKind: OrderKind): List<MatchResult> {
        return withStockLock(stockId) {
            getOrCreate(stockId).addOrder(entry, orderType, orderKind)
        }
    }

    fun cancelOrder(stockId: String, orderId: String): Boolean {
        return withStockLock(stockId) {
            getOrCreate(stockId).cancelOrder(orderId)
        }
    }

    fun getSnapshot(stockId: String, depth: Int = 10): OrderBookSnapshot {
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

    private fun getOrCreate(stockId: String): OrderBook {
        return orderBooks.getOrPut(stockId) {
            val orderBook = OrderBook(stockId)
            try {
                val bidEntries = orderBookCachePort.loadEntries(stockId, OrderType.BUY)
                val askEntries = orderBookCachePort.loadEntries(stockId, OrderType.SELL)
                if (bidEntries.isNotEmpty()) orderBook.restore(bidEntries, OrderType.BUY)
                if (askEntries.isNotEmpty()) orderBook.restore(askEntries, OrderType.SELL)
                if (bidEntries.isNotEmpty() || askEntries.isNotEmpty()) {
                    log.info("호가창 복구 완료: stockId={}, 매수={}, 매도={}", stockId, bidEntries.size, askEntries.size)
                }
            } catch (e: Exception) {
                log.warn("호가창 캐시 복구 실패: stockId={}, 원인={}", stockId, e.message)
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
