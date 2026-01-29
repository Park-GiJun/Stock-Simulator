package com.stocksimulator.schedulerservice.scheduler

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.InvestorType
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.common.event.InvestorCreatedEvent
import com.stocksimulator.common.event.KafkaTopics
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

/**
 * 투자자 생성 스케줄러
 * - 개인 투자자: 10분마다 1~3명 생성
 * - 기관 투자자: 2시간마다 50% 확률로 1개 생성
 */
@Component
class InvestorGenerationScheduler(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 10분마다 개인 투자자 생성 (현실 시간 기준)
    @Scheduled(fixedRate = 600000) // 10분 = 600,000 ms
    fun generateIndividualInvestors() {
        val count = Random.nextInt(1, 4) // 1~3명
        repeat(count) {
            val investmentStyle = InvestmentStyle.values().random()
            val capital = generateIndividualCapital()
            
            val event = InvestorCreatedEvent(
                investorId = "NPC${UUID.randomUUID()}",
                investorType = InvestorType.INDIVIDUAL.name,
                investmentStyle = investmentStyle.name,
                initialCapital = capital,
                parameters = generateIndividualParams(investmentStyle, capital)
            )

            kafkaTemplate.send(KafkaTopics.INVESTOR_CREATED, event.investorId, event)

            logger.info("""
                👤 개인 투자자 생성
                - ID: ${event.investorId}
                - 투자 성향: ${investmentStyle.displayName}
                - 초기 자본금: ${String.format("%,d", capital)}원
            """.trimIndent())
        }
    }

    // 2시간마다 기관 투자자 생성 (현실 시간 기준)
    @Scheduled(fixedRate = 7200000) // 2시간 = 7,200,000 ms
    fun generateInstitutionalInvestor() {
        if (Random.nextDouble() < 0.5) { // 50% 확률
            val investmentStyle = listOf(
                InvestmentStyle.AGGRESSIVE,
                InvestmentStyle.STABLE,
                InvestmentStyle.VALUE
            ).random()
            
            val capital = generateInstitutionCapital()

            val event = InvestorCreatedEvent(
                investorId = "INST${UUID.randomUUID()}",
                investorType = InvestorType.INSTITUTION.name,
                investmentStyle = investmentStyle.name,
                initialCapital = capital,
                parameters = generateInstitutionParams(investmentStyle, capital)
            )

            kafkaTemplate.send(KafkaTopics.INVESTOR_CREATED, event.investorId, event)

            logger.info("""
                🏢 기관 투자자 생성
                - ID: ${event.investorId}
                - 투자 성향: ${investmentStyle.displayName}
                - 초기 자본금: ${String.format("%,d", capital)}원
            """.trimIndent())
        }
    }

    /**
     * 개인 투자자 자본금 생성 (20만 ~ 1억)
     */
    private fun generateIndividualCapital(): Long {
        return when (Random.nextDouble()) {
            in 0.0..0.4 -> Random.nextLong(200_000, 1_000_000)        // 40%: 20만~100만
            in 0.4..0.7 -> Random.nextLong(1_000_000, 10_000_000)     // 30%: 100만~1,000만
            in 0.7..0.9 -> Random.nextLong(10_000_000, 50_000_000)    // 20%: 1,000만~5,000만
            else -> Random.nextLong(50_000_000, 100_000_000)          // 10%: 5,000만~1억
        }
    }

    /**
     * 기관 투자자 자본금 생성 (10억 ~ 1조)
     */
    private fun generateInstitutionCapital(): Long {
        return when (Random.nextDouble()) {
            in 0.0..0.5 -> Random.nextLong(1_000_000_000, 10_000_000_000)       // 50%: 10억~100억
            in 0.5..0.8 -> Random.nextLong(10_000_000_000, 100_000_000_000)     // 30%: 100억~1,000억
            else -> Random.nextLong(100_000_000_000, 1_000_000_000_000)         // 20%: 1,000억~1조
        }
    }

    /**
     * 개인 투자자 파라미터 생성
     */
    private fun generateIndividualParams(style: InvestmentStyle, capital: Long): Map<String, Any> {
        val weeklySalary = (capital * 0.05).toLong() // 자본금의 5%

        return mapOf(
            "weeklySalary" to weeklySalary,
            "riskTolerance" to when (style) {
                InvestmentStyle.AGGRESSIVE -> Random.nextDouble(0.7, 1.0)
                InvestmentStyle.STABLE -> Random.nextDouble(0.1, 0.4)
                InvestmentStyle.VALUE -> Random.nextDouble(0.3, 0.6)
                InvestmentStyle.RANDOM -> Random.nextDouble(0.0, 1.0)
            },
            "preferredSectors" to generateRandomSectors(2),
            "avoidSectors" to generateRandomSectors(1),
            "rebalancingCycle" to Random.nextInt(1, 8), // 게임 1~7일
            "tradingFrequency" to when (style) {
                InvestmentStyle.AGGRESSIVE -> TradingFrequency.HIGH.name
                InvestmentStyle.STABLE -> TradingFrequency.LOW.name
                else -> TradingFrequency.MEDIUM.name
            },
            "fomo" to when (style) {
                InvestmentStyle.AGGRESSIVE -> Random.nextDouble(0.6, 1.0)
                else -> Random.nextDouble(0.0, 0.4)
            },
            "panicSell" to when (style) {
                InvestmentStyle.STABLE -> Random.nextDouble(0.0, 0.3)
                else -> Random.nextDouble(0.3, 0.7)
            }
        )
    }

    /**
     * 기관 투자자 파라미터 생성
     */
    private fun generateInstitutionParams(style: InvestmentStyle, capital: Long): Map<String, Any> {
        val dailyIncome = (capital * 0.01).toLong() // 자본금의 1%

        return mapOf(
            "dailyIncome" to dailyIncome,
            "riskTolerance" to when (style) {
                InvestmentStyle.AGGRESSIVE -> Random.nextDouble(0.6, 0.9)
                InvestmentStyle.STABLE -> Random.nextDouble(0.2, 0.4)
                InvestmentStyle.VALUE -> Random.nextDouble(0.3, 0.6)
                else -> Random.nextDouble(0.3, 0.7)
            },
            "preferredSectors" to generateRandomSectors(3),
            "avoidSectors" to generateRandomSectors(2),
            "rebalancingCycle" to Random.nextInt(3, 15), // 게임 3~14일
            "maxPositionRatio" to when (style) {
                InvestmentStyle.AGGRESSIVE -> Random.nextDouble(0.15, 0.25)
                InvestmentStyle.STABLE -> Random.nextDouble(0.05, 0.10)
                InvestmentStyle.VALUE -> Random.nextDouble(0.10, 0.20)
                else -> Random.nextDouble(0.10, 0.15)
            },
            "tradingFrequency" to when (style) {
                InvestmentStyle.AGGRESSIVE -> TradingFrequency.HIGH.name
                InvestmentStyle.STABLE -> TradingFrequency.LOW.name
                else -> TradingFrequency.MEDIUM.name
            }
        )
    }

    /**
     * 랜덤 섹터 선택
     */
    private fun generateRandomSectors(count: Int): List<String> {
        return Sector.values().toList().shuffled().take(count).map { it.name }
    }
}
