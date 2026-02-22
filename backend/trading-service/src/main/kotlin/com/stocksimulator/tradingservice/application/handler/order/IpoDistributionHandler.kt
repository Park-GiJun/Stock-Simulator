package com.stocksimulator.tradingservice.application.handler.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType
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

@Service
class IpoDistributionHandler(
    private val portfolioPersistencePort: PortfolioPersistencePort,
    private val orderPersistencePort: OrderPersistencePort,
    private val orderBookRegistry: OrderBookRegistry,
    private val tradingEventPort: TradingEventPort,
    @Value("\${ipo.price-levels:20}") private val priceLevels: Int,
    @Value("\${ipo.price-spread-percent:15}") private val priceSpreadPercent: Int
) : SeedIpoOrderBookUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun distributeIpoShares(event: StockListedEvent) {
        log.info(
            "IPO 배분 시작: stockId={}, stockName={}, sector={}, basePrice={}, totalShares={}",
            event.stockId, event.stockName, event.sector, event.basePrice, event.totalShares
        )

        val totalShares = event.totalShares
        val stockId = event.stockId
        val basePrice = event.basePrice

        // 1. 관리기관(Market Maker) 포트폴리오 생성 - 전체 주식 보유
        val marketMakerPortfolio = PortfolioModel(
            investorId = SystemConstants.SYSTEM_MARKET_MAKER_ID,
            investorType = "MARKET_MAKER",
            stockId = stockId,
            quantity = totalShares,
            averagePrice = basePrice,
            totalInvested = totalShares * basePrice
        )
        portfolioPersistencePort.save(marketMakerPortfolio)
        log.debug("관리기관 포트폴리오 생성: stockId={}, 보유수량={}주", stockId, totalShares)

        // 2. 전량 매도호가 등록 (가격단계별 분산)
        seedOrderBook(stockId, basePrice, totalShares)

        log.info(
            "IPO 배분 완료: stockId={}, 관리기관={}주 전량 매도호가 등록",
            stockId, totalShares
        )
    }

    private fun seedOrderBook(stockId: String, basePrice: Long, totalShares: Long) {
        // basePrice ~ basePrice × (1 + spreadPercent/100) 까지 priceLevels 단계
        val maxPrice = basePrice + (basePrice * priceSpreadPercent / 100)

        val priceLevelsList = if (priceLevels <= 1) {
            listOf(PriceUtil.adjustPriceUp(basePrice))
        } else {
            val step = (maxPrice - basePrice).toDouble() / (priceLevels - 1)
            (0 until priceLevels).map { i ->
                PriceUtil.adjustPriceUp(basePrice + (step * i).toLong())
            }.distinct()
        }

        val actualLevels = priceLevelsList.size

        // 전방 가중 분배: 낮은 가격에 더 많은 수량 배분
        val weights = (actualLevels downTo 1).toList()
        val totalWeight = weights.sum()

        val ordersPerLevel = 5 // 각 호가에 3~5개 매도 주문
        val allOrders = mutableListOf<OrderModel>()
        val allEntries = mutableListOf<OrderEntryVo>()

        priceLevelsList.forEachIndexed { index, price ->
            val levelShares = (totalShares * weights[index] / totalWeight)
            if (levelShares <= 0) return@forEachIndexed

            // 호가별 3~5개 매도 주문으로 분할
            val sharesPerOrder = levelShares / ordersPerLevel
            var levelRemainder = levelShares % ordersPerLevel

            for (orderIdx in 0 until ordersPerLevel) {
                val orderShares = sharesPerOrder + if (levelRemainder-- > 0) 1 else 0
                if (orderShares <= 0) continue

                val order = OrderModel.create(
                    userId = SystemConstants.SYSTEM_MARKET_MAKER_ID,
                    stockId = stockId,
                    orderType = OrderType.SELL,
                    orderKind = OrderKind.LIMIT,
                    price = price,
                    quantity = orderShares,
                    investorType = "MARKET_MAKER"
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

        // Redis 캐시 + DB 동기화 저장
        orderBookRegistry.persistToCacheAndDb(stockId)

        // orderbook.updated 이벤트 발행
        val snapshot = orderBookRegistry.getSnapshot(stockId)
        tradingEventPort.publishOrderBookUpdated(snapshot)

        log.debug(
            "매도호가 등록 완료: stockId={}, 주문수={}, 호가단계={}, 가격범위={}~{}",
            stockId, allOrders.size, actualLevels, priceLevelsList.first(), priceLevelsList.last()
        )
    }
}
