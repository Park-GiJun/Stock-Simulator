package com.stocksimulator.tradingservice.domain.service

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.tradingservice.domain.vo.MatchResult
import com.stocksimulator.tradingservice.domain.vo.OrderBookSnapshot
import com.stocksimulator.tradingservice.domain.vo.OrderEntry
import com.stocksimulator.tradingservice.domain.vo.PriceLevel
import java.time.Instant
import java.util.TreeMap
import java.util.UUID

class OrderBook(
    val stockId: String
) {
    // 매수호가: 높은 가격 우선 (내림차순)
    private val bids: TreeMap<Long, MutableList<OrderEntry>> = TreeMap(reverseOrder())
    // 매도호가: 낮은 가격 우선 (오름차순)
    private val asks: TreeMap<Long, MutableList<OrderEntry>> = TreeMap()
    // 주문ID → OrderEntry 인덱스 (O(1) 취소용)
    private val orderIndex: MutableMap<String, OrderEntry> = mutableMapOf()

    fun addOrder(entry: OrderEntry, orderType: OrderType, orderKind: OrderKind): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()

        when (orderKind) {
            OrderKind.LIMIT -> {
                matchOrder(entry, orderType, matches)
                // 잔량이 남으면 호가창에 등록
                if (entry.remainingQuantity > 0) {
                    addToBook(entry, orderType)
                }
            }
            OrderKind.MARKET -> {
                matchOrder(entry, orderType, matches)
                // 시장가는 잔량을 호가창에 등록하지 않음
            }
        }

        return matches
    }

    private fun matchOrder(entry: OrderEntry, orderType: OrderType, matches: MutableList<MatchResult>) {
        val oppositeBook = if (orderType == OrderType.BUY) asks else bids

        val iterator = oppositeBook.entries.iterator()
        while (iterator.hasNext() && entry.remainingQuantity > 0) {
            val (price, orders) = iterator.next()

            // 지정가인 경우 가격 조건 확인
            if (entry.price > 0) { // price > 0 means LIMIT order
                if (orderType == OrderType.BUY && price > entry.price) break
                if (orderType == OrderType.SELL && price < entry.price) break
            }

            val orderIterator = orders.iterator()
            while (orderIterator.hasNext() && entry.remainingQuantity > 0) {
                val oppositeEntry = orderIterator.next()
                val matchQuantity = minOf(entry.remainingQuantity, oppositeEntry.remainingQuantity)

                val (buyOrderId, sellOrderId, buyUserId, sellUserId) = if (orderType == OrderType.BUY) {
                    listOf(entry.orderId, oppositeEntry.orderId, entry.userId, oppositeEntry.userId)
                } else {
                    listOf(oppositeEntry.orderId, entry.orderId, oppositeEntry.userId, entry.userId)
                }

                matches.add(
                    MatchResult(
                        tradeId = UUID.randomUUID().toString(),
                        buyOrderId = buyOrderId,
                        sellOrderId = sellOrderId,
                        buyUserId = buyUserId,
                        sellUserId = sellUserId,
                        stockId = stockId,
                        price = price,
                        quantity = matchQuantity,
                        matchedAt = Instant.now()
                    )
                )

                entry.remainingQuantity -= matchQuantity
                oppositeEntry.remainingQuantity -= matchQuantity

                if (oppositeEntry.remainingQuantity == 0L) {
                    orderIterator.remove()
                    orderIndex.remove(oppositeEntry.orderId)
                }
            }

            if (orders.isEmpty()) {
                iterator.remove()
            }
        }
    }

    private fun addToBook(entry: OrderEntry, orderType: OrderType) {
        val book = if (orderType == OrderType.BUY) bids else asks
        book.getOrPut(entry.price) { mutableListOf() }.add(entry)
        orderIndex[entry.orderId] = entry
    }

    fun cancelOrder(orderId: String): Boolean {
        val entry = orderIndex.remove(orderId) ?: return false

        // 양쪽 호가창에서 검색하여 제거
        removeFromBook(bids, entry)
        removeFromBook(asks, entry)

        return true
    }

    private fun removeFromBook(book: TreeMap<Long, MutableList<OrderEntry>>, entry: OrderEntry) {
        val orders = book[entry.price] ?: return
        orders.remove(entry)
        if (orders.isEmpty()) {
            book.remove(entry.price)
        }
    }

    fun getSnapshot(depth: Int = 10): OrderBookSnapshot {
        val bidLevels = bids.entries.take(depth).map { (price, orders) ->
            PriceLevel(
                price = price,
                quantity = orders.sumOf { it.remainingQuantity },
                orderCount = orders.size
            )
        }

        val askLevels = asks.entries.take(depth).map { (price, orders) ->
            PriceLevel(
                price = price,
                quantity = orders.sumOf { it.remainingQuantity },
                orderCount = orders.size
            )
        }

        val bestBid = bids.firstEntry()?.key
        val bestAsk = asks.firstEntry()?.key
        val spread = if (bestBid != null && bestAsk != null) bestAsk - bestBid else null

        return OrderBookSnapshot(
            stockId = stockId,
            bids = bidLevels,
            asks = askLevels,
            bestBid = bestBid,
            bestAsk = bestAsk,
            spread = spread
        )
    }

    fun getAllEntries(): Pair<List<Pair<OrderEntry, OrderType>>, List<Pair<OrderEntry, OrderType>>> {
        val bidEntries = bids.values.flatten().map { it to OrderType.BUY }
        val askEntries = asks.values.flatten().map { it to OrderType.SELL }
        return bidEntries to askEntries
    }

    fun restore(entries: List<OrderEntry>, side: OrderType) {
        val book = if (side == OrderType.BUY) bids else asks
        for (entry in entries) {
            book.getOrPut(entry.price) { mutableListOf() }.add(entry)
            orderIndex[entry.orderId] = entry
        }
    }

    fun hasOrder(orderId: String): Boolean = orderIndex.containsKey(orderId)
}
