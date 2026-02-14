package com.stocksimulator.schedulerservice.application.handler

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.schedulerservice.application.port.out.CompanyNameGeneratePort
import com.stocksimulator.schedulerservice.application.port.out.StockEventPublishPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class StockListingHandler(
    private val stockEventPublishPort: StockEventPublishPort,
    private val companyNameGeneratePort: CompanyNameGeneratePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun initiateIPO() {
        val sector = Sector.entries.toTypedArray().random()
        val marketCapGrade = MarketCapGrade.entries.toTypedArray().random()
        val basePrice = generateBasePrice()
        val totalShares = calculateTotalShares(marketCapGrade, basePrice)

        val event = StockListedEvent(
            stockId = companyNameGeneratePort.generateStockCode(),
            stockName = companyNameGeneratePort.generate(sector),
            sector = sector.name,
            basePrice = basePrice,
            totalShares = totalShares,
            marketCapGrade = marketCapGrade.name
        )

        stockEventPublishPort.publishStockListed(event)

        log.info(
            "IPO 발생 - 종목: {} ({}), 산업: {}, 기준가: {}원, 시총등급: {}, 발행주식수: {}주",
            event.stockName, event.stockId, sector.displayName,
            event.basePrice, marketCapGrade.displayName, event.totalShares
        )
    }

    fun initiateDelisting() {
        val stockId = "A${Random.nextInt(100000, 999999)}"
        val reasons = listOf(
            "시가총액 50억 미만 지속",
            "거래량 부족 (30일 평균 1,000주 미만)",
            "주가 액면가 이하 지속",
            "감사의견 거절",
            "경영 악화"
        )

        val event = StockDelistedEvent(
            stockId = stockId,
            stockName = "상장폐지 대상",
            reason = reasons.random(),
            finalPrice = Random.nextLong(500, 5000)
        )

        stockEventPublishPort.publishStockDelisted(event)

        log.warn(
            "상장폐지 발생 - 종목: {} ({}), 사유: {}, 최종가: {}원",
            event.stockName, event.stockId, event.reason, event.finalPrice
        )
    }

    private fun generateBasePrice(): Long {
        return when {
            Random.nextDouble() < 0.3 -> Random.nextLong(1_000, 5_000)
            Random.nextDouble() < 0.7 -> Random.nextLong(5_000, 50_000)
            else -> Random.nextLong(50_000, 500_000)
        }
    }

    private fun calculateTotalShares(grade: MarketCapGrade, basePrice: Long): Long {
        val targetMarketCap = when (grade) {
            MarketCapGrade.SMALL -> Random.nextLong(1_000_000_000, 10_000_000_000)
            MarketCapGrade.MID -> Random.nextLong(10_000_000_000, 100_000_000_000)
            MarketCapGrade.LARGE -> Random.nextLong(100_000_000_000, 1_000_000_000_000)
        }
        return targetMarketCap / basePrice
    }
}
