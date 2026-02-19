package com.stocksimulator.tradingservice.infrastructure.adapter.out.cache

import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.application.port.out.order.OrderBookCachePort
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshotVo
import com.stocksimulator.tradingservice.domain.vo.OrderEntryVo
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RedisOrderBookCacheAdapter(
    private val redissonClient: RedissonClient
) : OrderBookCachePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveSnapshot(stockId: String, snapshot: OrderBookSnapshotVo) {
        try {
            val bucket = redissonClient.getBucket<Map<String, Any>>("orderbook:$stockId:snapshot")
            bucket.set(mapOf(
                "stockId" to snapshot.stockId,
                "bestBid" to (snapshot.bestBid?.toString() ?: ""),
                "bestAsk" to (snapshot.bestAsk?.toString() ?: ""),
                "spread" to (snapshot.spread?.toString() ?: ""),
                "timestamp" to snapshot.timestamp.toString()
            ))
        } catch (e: Exception) {
            log.warn("Redis 스냅샷 저장 실패: stockId={}, 원인={}", stockId, e.message)
        }
    }

    override fun loadEntries(stockId: String, side: OrderType): List<OrderEntryVo> {
        try {
            val sideName = if (side == OrderType.BUY) "bids" else "asks"
            val map = redissonClient.getMap<String, String>("orderbook:$stockId:$sideName")
            if (map.isEmpty()) return emptyList()

            return map.entries.mapNotNull { (orderId, value) ->
                try {
                    val parts = value.split("|")
                    if (parts.size >= 4) {
                        OrderEntryVo(
                            orderId = orderId,
                            userId = parts[0],
                            price = parts[1].toLong(),
                            remainingQuantity = parts[2].toLong(),
                            timestamp = Instant.parse(parts[3])
                        )
                    } else null
                } catch (e: Exception) {
                    log.warn("주문 엔트리 파싱 실패: orderId={}", orderId)
                    null
                }
            }
        } catch (e: Exception) {
            log.warn("Redis 엔트리 로드 실패: stockId={}, side={}, 원인={}", stockId, side, e.message)
            return emptyList()
        }
    }

    override fun saveEntries(stockId: String, entries: List<OrderEntryVo>, side: OrderType) {
        try {
            val sideName = if (side == OrderType.BUY) "bids" else "asks"
            val map = redissonClient.getMap<String, String>("orderbook:$stockId:$sideName")
            map.clear()

            if (entries.isNotEmpty()) {
                val entryMap = entries.associate { entry ->
                    entry.orderId to "${entry.userId}|${entry.price}|${entry.remainingQuantity}|${entry.timestamp}"
                }
                map.putAll(entryMap)
            }
        } catch (e: Exception) {
            log.warn("Redis 엔트리 저장 실패: stockId={}, side={}, 원인={}", stockId, side, e.message)
        }
    }

    override fun deleteEntries(stockId: String, orderIds: List<String>) {
        try {
            for (sideName in listOf("bids", "asks")) {
                val map = redissonClient.getMap<String, String>("orderbook:$stockId:$sideName")
                for (orderId in orderIds) {
                    map.remove(orderId)
                }
            }
        } catch (e: Exception) {
            log.warn("Redis 엔트리 삭제 실패: stockId={}, 원인={}", stockId, e.message)
        }
    }
}
