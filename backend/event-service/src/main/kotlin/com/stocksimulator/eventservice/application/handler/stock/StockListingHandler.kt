package com.stocksimulator.eventservice.application.handler.stock

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.eventservice.application.port.out.stock.CompanyNameGeneratePort
import com.stocksimulator.eventservice.application.port.out.stock.StockEventPublishPort
import com.stocksimulator.eventservice.application.port.out.stock.StockQueryPort
import com.stocksimulator.common.util.PriceUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.roundToInt
import kotlin.random.Random

@Service
class StockListingHandler(
    private val stockEventPublishPort: StockEventPublishPort,
    private val companyNameGeneratePort: CompanyNameGeneratePort,
    private val stockQueryPort: StockQueryPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun initiateIPO() {
        val sector = Sector.entries.toTypedArray().random()
        val marketCapGrade = MarketCapGrade.entries.toTypedArray().random()
        val basePrice = generateBasePrice()
        val totalShares = calculateTotalShares(marketCapGrade, basePrice)
        val volatility = generateVolatility(sector, marketCapGrade)
        val per = generatePER(sector, marketCapGrade)
        val dividendRate = generateDividendRate(sector, marketCapGrade)
        val growthRate = generateGrowthRate(sector, marketCapGrade)
        val eventSensitivity = generateEventSensitivity(sector, marketCapGrade)

        val event = StockListedEvent(
            stockId = companyNameGeneratePort.generateStockCode(),
            stockName = companyNameGeneratePort.generate(sector),
            sector = sector.name,
            basePrice = basePrice,
            totalShares = totalShares,
            marketCapGrade = marketCapGrade.name,
            volatility = volatility,
            per = per,
            dividendRate = dividendRate,
            growthRate = growthRate,
            eventSensitivity = eventSensitivity
        )

        stockEventPublishPort.publishStockListed(event)

        log.info(
            "IPO 발생 - 종목: {} ({}), 산업: {}, 기준가: {}원, 시총등급: {}, 발행주식수: {}주, " +
                "변동성: {}, PER: {}, 배당률: {}%, 성장률: {}%, 이벤트민감도: {}",
            event.stockName, event.stockId, sector.displayName,
            event.basePrice, marketCapGrade.displayName, event.totalShares,
            volatility, per, dividendRate, growthRate, eventSensitivity
        )
    }

    fun initiateDelisting() {
        val stock = stockQueryPort.getRandomListedStock()
        if (stock == null) {
            log.warn("상장폐지 실패 - 상장된 종목이 없음")
            return
        }

        val reasons = listOf(
            "시가총액 50억 미만 지속",
            "거래량 부족 (30일 평균 1,000주 미만)",
            "주가 액면가 이하 지속",
            "감사의견 거절",
            "경영 악화"
        )

        val event = StockDelistedEvent(
            stockId = stock.stockId,
            stockName = stock.stockName,
            reason = reasons.random(),
            finalPrice = stock.currentPrice
        )

        stockEventPublishPort.publishStockDelisted(event)

        log.warn(
            "상장폐지 발생 - 종목: {} ({}), 사유: {}, 최종가: {}원",
            event.stockName, event.stockId, event.reason, event.finalPrice
        )
    }

    private fun generateBasePrice(): Long {
        val rawPrice = when {
            Random.nextDouble() < 0.3 -> Random.nextLong(1_000, 5_000)
            Random.nextDouble() < 0.7 -> Random.nextLong(5_000, 50_000)
            else -> Random.nextLong(50_000, 500_000)
        }
        return PriceUtil.adjustPriceDown(rawPrice)
    }

    private fun calculateTotalShares(grade: MarketCapGrade, basePrice: Long): Long {
        val targetMarketCap = when (grade) {
            MarketCapGrade.SMALL -> Random.nextLong(1_000_000_000, 10_000_000_000)
            MarketCapGrade.MID -> Random.nextLong(10_000_000_000, 100_000_000_000)
            MarketCapGrade.LARGE -> Random.nextLong(100_000_000_000, 1_000_000_000_000)
        }
        return targetMarketCap / basePrice
    }

    private fun generateVolatility(sector: Sector, grade: MarketCapGrade): Long {
        val base = when (sector) {
            Sector.IT -> Random.nextLong(40, 80)
            Sector.REAL_ESTATE -> Random.nextLong(10, 30)
            Sector.LUXURY -> Random.nextLong(30, 60)
            Sector.AGRICULTURE -> Random.nextLong(15, 35)
            Sector.MANUFACTURING -> Random.nextLong(20, 40)
            Sector.SERVICE -> Random.nextLong(25, 50)
            Sector.FOOD -> Random.nextLong(15, 35)
        }
        return when (grade) {
            MarketCapGrade.SMALL -> base + Random.nextLong(10, 20)
            MarketCapGrade.MID -> base
            MarketCapGrade.LARGE -> (base * 0.7).toLong()
        }
    }

    private fun generatePER(sector: Sector, grade: MarketCapGrade): Double {
        val base = when (sector) {
            Sector.IT -> Random.nextDouble(20.0, 60.0)
            Sector.REAL_ESTATE -> Random.nextDouble(8.0, 20.0)
            Sector.LUXURY -> Random.nextDouble(15.0, 40.0)
            Sector.AGRICULTURE -> Random.nextDouble(6.0, 15.0)
            Sector.MANUFACTURING -> Random.nextDouble(8.0, 18.0)
            Sector.SERVICE -> Random.nextDouble(10.0, 25.0)
            Sector.FOOD -> Random.nextDouble(8.0, 20.0)
        }
        val adjusted = when (grade) {
            MarketCapGrade.SMALL -> base * Random.nextDouble(0.6, 1.0)
            MarketCapGrade.MID -> base
            MarketCapGrade.LARGE -> base * Random.nextDouble(1.0, 1.3)
        }
        return (adjusted * 100.0).roundToInt() / 100.0
    }

    private fun generateDividendRate(sector: Sector, grade: MarketCapGrade): Double {
        val base = when (sector) {
            Sector.IT -> Random.nextDouble(0.0, 1.5)
            Sector.REAL_ESTATE -> Random.nextDouble(2.0, 6.0)
            Sector.LUXURY -> Random.nextDouble(0.5, 2.0)
            Sector.AGRICULTURE -> Random.nextDouble(1.0, 3.0)
            Sector.MANUFACTURING -> Random.nextDouble(1.5, 4.0)
            Sector.SERVICE -> Random.nextDouble(1.0, 3.0)
            Sector.FOOD -> Random.nextDouble(1.5, 4.0)
        }
        val adjusted = when (grade) {
            MarketCapGrade.SMALL -> base * Random.nextDouble(0.3, 0.8)
            MarketCapGrade.MID -> base
            MarketCapGrade.LARGE -> base * Random.nextDouble(1.0, 1.5)
        }
        return (adjusted * 100.0).roundToInt() / 100.0
    }

    private fun generateGrowthRate(sector: Sector, grade: MarketCapGrade): Double {
        val base = when (sector) {
            Sector.IT -> Random.nextDouble(5.0, 30.0)
            Sector.REAL_ESTATE -> Random.nextDouble(-5.0, 10.0)
            Sector.LUXURY -> Random.nextDouble(0.0, 15.0)
            Sector.AGRICULTURE -> Random.nextDouble(-3.0, 8.0)
            Sector.MANUFACTURING -> Random.nextDouble(-2.0, 12.0)
            Sector.SERVICE -> Random.nextDouble(0.0, 15.0)
            Sector.FOOD -> Random.nextDouble(-2.0, 10.0)
        }
        val adjusted = when (grade) {
            MarketCapGrade.SMALL -> base + Random.nextDouble(3.0, 10.0)
            MarketCapGrade.MID -> base
            MarketCapGrade.LARGE -> base * Random.nextDouble(0.5, 0.9)
        }
        return (adjusted * 100.0).roundToInt() / 100.0
    }

    private fun generateEventSensitivity(sector: Sector, grade: MarketCapGrade): Double {
        val base = when (sector) {
            Sector.IT -> Random.nextDouble(0.5, 0.9)
            Sector.REAL_ESTATE -> Random.nextDouble(0.6, 0.95)
            Sector.LUXURY -> Random.nextDouble(0.4, 0.8)
            Sector.AGRICULTURE -> Random.nextDouble(0.3, 0.7)
            Sector.MANUFACTURING -> Random.nextDouble(0.3, 0.6)
            Sector.SERVICE -> Random.nextDouble(0.4, 0.7)
            Sector.FOOD -> Random.nextDouble(0.2, 0.5)
        }
        val adjusted = when (grade) {
            MarketCapGrade.SMALL -> (base * Random.nextDouble(1.1, 1.4)).coerceAtMost(1.0)
            MarketCapGrade.MID -> base
            MarketCapGrade.LARGE -> base * Random.nextDouble(0.6, 0.9)
        }
        return (adjusted * 100.0).roundToInt() / 100.0
    }
}
