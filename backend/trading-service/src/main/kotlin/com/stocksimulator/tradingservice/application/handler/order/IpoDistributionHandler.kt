package com.stocksimulator.tradingservice.application.handler.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.common.util.PriceUtil
import com.stocksimulator.tradingservice.application.port.`in`.order.SeedIpoOrderBookUseCase
import com.stocksimulator.tradingservice.application.port.out.order.OrderPersistencePort
import com.stocksimulator.tradingservice.application.port.out.order.TradingEventPort
import com.stocksimulator.tradingservice.application.port.out.portfolio.PortfolioPersistencePort
import com.stocksimulator.tradingservice.domain.SystemConstants
import com.stocksimulator.tradingservice.domain.model.OrderModel
import com.stocksimulator.tradingservice.domain.model.PortfolioModel
import com.stocksimulator.tradingservice.domain.vo.OrderEntryVo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class IpoDistributionHandler(
    private val portfolioPersistencePort: PortfolioPersistencePort,
    private val orderPersistencePort: OrderPersistencePort,
    private val orderBookRegistry: OrderBookRegistry,
    private val tradingEventPort: TradingEventPort,
    @Value("\${ipo.institution-ratio:0.50}") private val institutionRatio: Double,
    @Value("\${ipo.npc-ratio:0.25}") private val npcRatio: Double,
    @Value("\${ipo.float-ratio:0.25}") private val floatRatio: Double,
    @Value("\${ipo.price-levels:10}") private val priceLevels: Int
) : SeedIpoOrderBookUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun distributeIpoShares(event: StockListedEvent) {
        log.info(
            "IPO 배분 시작: stockId={}, stockName={}, sector={}, basePrice={}, totalShares={}",
            event.stockId, event.stockName, event.sector, event.basePrice, event.totalShares
        )

        val totalShares = event.totalShares
        val institutionShares = (totalShares * institutionRatio).toLong()
        val npcShares = (totalShares * npcRatio).toLong()
        val marketShares = totalShares - institutionShares - npcShares

        // 1. 기관 배분 (50%)
        distributeToInstitutions(event.stockId, event.basePrice, institutionShares)

        // 2. NPC 배분 (25%) - 섹터 기반
        distributeToNpcs(event.stockId, event.sector, event.basePrice, npcShares)

        // 3. 매도호가 등록 (25%)
        seedOrderBook(event.stockId, event.basePrice, marketShares)

        log.info(
            "IPO 배분 완료: stockId={}, 기관={}주, NPC={}주, 매도호가={}주",
            event.stockId, institutionShares, npcShares, marketShares
        )
    }

    private fun distributeToInstitutions(stockId: String, basePrice: Long, totalShares: Long) {
        val institutions = SystemConstants.SYSTEM_INSTITUTIONS
        val sharesPerInstitution = totalShares / institutions.size
        var remainder = totalShares % institutions.size

        val portfolios = institutions.map { institutionId ->
            val shares = sharesPerInstitution + if (remainder-- > 0) 1 else 0
            PortfolioModel(
                investorId = institutionId,
                investorType = "INSTITUTION",
                stockId = stockId,
                quantity = shares,
                averagePrice = basePrice,
                totalInvested = shares * basePrice
            )
        }

        portfolioPersistencePort.saveAll(portfolios)
        log.debug("기관 배분 완료: stockId={}, 기관수={}", stockId, institutions.size)
    }

    private fun distributeToNpcs(stockId: String, sectorStr: String, basePrice: Long, totalShares: Long) {
        val sector = try {
            Sector.valueOf(sectorStr)
        } catch (e: IllegalArgumentException) {
            null
        }

        // 섹터 매칭 NPC 필터링
        val matchingNpcs = if (sector != null) {
            SystemConstants.NPC_SECTOR_PREFERENCES
                .filter { (_, sectors) -> sector in sectors }
                .keys.toList()
        } else {
            emptyList()
        }

        // 매칭되는 NPC가 없으면 전체 NPC에 배분
        val targetNpcs = matchingNpcs.ifEmpty { SystemConstants.SYSTEM_NPCS }

        val sharesPerNpc = totalShares / targetNpcs.size
        var remainder = totalShares % targetNpcs.size

        val portfolios = targetNpcs.map { npcId ->
            val shares = sharesPerNpc + if (remainder-- > 0) 1 else 0
            PortfolioModel(
                investorId = npcId,
                investorType = "NPC",
                stockId = stockId,
                quantity = shares,
                averagePrice = basePrice,
                totalInvested = shares * basePrice
            )
        }

        portfolioPersistencePort.saveAll(portfolios)
        log.debug("NPC 배분 완료: stockId={}, NPC수={}, 섹터매칭={}", stockId, targetNpcs.size, matchingNpcs.isNotEmpty())
    }

    private fun seedOrderBook(stockId: String, basePrice: Long, totalShares: Long) {
        val tickSize = PriceUtil.getTickSize(basePrice)

        // 10단계 호가 생성 (basePrice, basePrice+1tick, ..., basePrice+9tick)
        val priceLevelsList = (0 until priceLevels).map { i ->
            PriceUtil.adjustPriceUp(basePrice + tickSize * i)
        }

        // 전방 가중 분배: 낮은 가격에 더 많은 수량 배분
        // 가중치: 10, 9, 8, ..., 1 → 합계 55
        val weights = (priceLevels downTo 1).toList()
        val totalWeight = weights.sum()

        val ordersPerLevel = 3 // 각 호가에 2~3개 매도 주문
        val allOrders = mutableListOf<OrderModel>()
        val allEntries = mutableListOf<OrderEntryVo>()

        priceLevelsList.forEachIndexed { index, price ->
            val levelShares = (totalShares * weights[index] / totalWeight)
            if (levelShares <= 0) return@forEachIndexed

            // 호가별 2~3개 매도 주문으로 분할
            val sharesPerOrder = levelShares / ordersPerLevel
            var levelRemainder = levelShares % ordersPerLevel

            for (orderIdx in 0 until ordersPerLevel) {
                val orderShares = sharesPerOrder + if (levelRemainder-- > 0) 1 else 0
                if (orderShares <= 0) continue

                val order = OrderModel.create(
                    userId = SystemConstants.SYSTEM_IPO_USER_ID,
                    stockId = stockId,
                    orderType = OrderType.SELL,
                    orderKind = OrderKind.LIMIT,
                    price = price,
                    quantity = orderShares,
                    investorType = "SYSTEM"
                )
                allOrders.add(order)

                allEntries.add(
                    OrderEntryVo(
                        orderId = order.orderId,
                        userId = order.userId,
                        price = price,
                        remainingQuantity = orderShares
                    )
                )
            }
        }

        // DB에 주문 저장
        orderPersistencePort.saveAll(allOrders)

        // 호가창에 직접 등록
        orderBookRegistry.seedIpoOrders(stockId, allEntries)

        // Redis 캐시 저장
        orderBookRegistry.persistToCache(stockId)

        // orderbook.updated 이벤트 발행
        val snapshot = orderBookRegistry.getSnapshot(stockId)
        tradingEventPort.publishOrderBookUpdated(snapshot)

        log.debug("매도호가 등록 완료: stockId={}, 주문수={}, 호가단계={}", stockId, allOrders.size, priceLevels)
    }
}
