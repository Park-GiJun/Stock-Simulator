package com.stocksimulator.schedulerservice.scheduler

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent
import com.stocksimulator.schedulerservice.service.CompanyNameGenerator
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 주식 상장/상장폐지 스케줄러
 * - IPO (신규 상장): 30분마다 30% 확률로 발생
 * - 상장폐지: 1시간마다 체크 (조건 만족 시)
 */
@Component
class StockListingScheduler(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val companyNameGenerator: CompanyNameGenerator
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 30분마다 IPO 확률 체크 (현실 시간 기준)
    @Scheduled(fixedRate = 1800000) // 30분 = 1,800,000 ms
    fun checkForIPO() {
        if (Random.nextDouble() < 0.3) { // 30% 확률
            initiateIPO()
        }
    }

    // 1시간마다 상장폐지 확률 체크
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000 ms
    fun checkForDelisting() {
        // 실제 구현 시 stock-service에서 조건에 맞는 주식 조회 필요
        // 여기서는 10% 확률로 랜덤 상장폐지 발생
        if (Random.nextDouble() < 0.1) {
            initiateDelisting()
        }
    }

    private fun initiateIPO() {
        val sector = Sector.values().random()
        val marketCapGrade = MarketCapGrade.values().random()
        val basePrice = generateBasePrice()
        val totalShares = calculateTotalShares(marketCapGrade, basePrice)

        val event = StockListedEvent(
            stockId = companyNameGenerator.generateStockCode(),
            stockName = companyNameGenerator.generate(sector),
            sector = sector.name,
            basePrice = basePrice,
            totalShares = totalShares,
            marketCapGrade = marketCapGrade.name
        )

        kafkaTemplate.send(KafkaTopics.STOCK_LISTED, event.stockId, event)
        
        logger.info("""
            📈 IPO 발생!
            - 종목: ${event.stockName} (${event.stockId})
            - 산업: ${sector.displayName}
            - 기준가: ${event.basePrice}원
            - 시총 등급: ${marketCapGrade.displayName}
            - 발행주식수: ${event.totalShares}주
        """.trimIndent())
    }

    private fun initiateDelisting() {
        // 실제로는 stock-service에서 조건에 맞는 주식을 조회해야 함
        // 여기서는 예시로 랜덤 생성
        val stockId = "A${Random.nextInt(100000, 999999)}"
        val stockName = "상장폐지예정 기업"
        val reasons = listOf(
            "시가총액 50억 미만 지속",
            "거래량 부족 (30일 평균 1,000주 미만)",
            "주가 액면가 이하 지속",
            "감사의견 거절",
            "경영 악화"
        )

        val event = StockDelistedEvent(
            stockId = stockId,
            stockName = stockName,
            reason = reasons.random(),
            finalPrice = Random.nextLong(500, 5000)
        )

        kafkaTemplate.send(KafkaTopics.STOCK_DELISTED, event.stockId, event)

        logger.warn("""
            📉 상장폐지 발생!
            - 종목: ${event.stockName} (${event.stockId})
            - 사유: ${event.reason}
            - 최종가: ${event.finalPrice}원
        """.trimIndent())
    }

    /**
     * 시가총액 등급에 따른 기준가 생성
     */
    private fun generateBasePrice(): Long {
        return when {
            Random.nextDouble() < 0.3 -> Random.nextLong(1_000, 5_000)      // 저가주
            Random.nextDouble() < 0.7 -> Random.nextLong(5_000, 50_000)    // 중가주
            else -> Random.nextLong(50_000, 500_000)                        // 고가주
        }
    }

    /**
     * 시가총액 등급과 기준가를 기반으로 발행주식수 계산
     */
    private fun calculateTotalShares(grade: MarketCapGrade, basePrice: Long): Long {
        val targetMarketCap = when (grade) {
            MarketCapGrade.SMALL -> Random.nextLong(1_000_000_000, 10_000_000_000)      // 10억~100억
            MarketCapGrade.MID -> Random.nextLong(10_000_000_000, 100_000_000_000)      // 100억~1,000억
            MarketCapGrade.LARGE -> Random.nextLong(100_000_000_000, 1_000_000_000_000) // 1,000억~1조
        }
        return targetMarketCap / basePrice
    }
}
