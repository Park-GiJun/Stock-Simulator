package com.stocksimulator.schedulerservice.application.handler

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.common.event.InstitutionCreatedEvent
import com.stocksimulator.schedulerservice.application.port.out.institution.InstitutionEventPublishPort
import com.stocksimulator.schedulerservice.application.port.out.institution.InstitutionNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.roundToInt
import kotlin.random.Random

@Service
class InstitutionCreationHandler(
    private val institutionEventPublishPort: InstitutionEventPublishPort,
    private val institutionNameGeneratePort: InstitutionNameGeneratePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 기관투자자 생성
     * - 유형: InstitutionType 중 랜덤
     * - 투자 성향: AGGRESSIVE, STABLE, VALUE (RANDOM 제외)
     * - 자본금: 10억 ~ 1조 KRW
     * - 일일 수입: 자본금의 1%
     * - 위험 허용도: 투자 성향에 따라 결정
     * - 선호 섹터: 1~3개 랜덤
     * - 거래 빈도: 투자 성향에 따라 결정
     */
    suspend fun createInstitution() {
        val institutionType = InstitutionType.entries.toTypedArray().random()
        val investmentStyle = listOf(
            InvestmentStyle.AGGRESSIVE,
            InvestmentStyle.STABLE,
            InvestmentStyle.VALUE
        ).random()

        val capital = Random.nextLong(1_000_000_000L, 1_000_000_000_000L)
        val dailyIncome = capital / 100

        val riskTolerance = generateRiskTolerance(investmentStyle)
        val preferredSectors = generatePreferredSectors()
        val tradingFrequency = determineTradingFrequency(investmentStyle)

        val institutionName = institutionNameGeneratePort.generate(institutionType)

        val event = InstitutionCreatedEvent(
            institutionName = institutionName,
            institutionType = institutionType.name,
            investmentStyle = investmentStyle.name,
            capital = capital,
            dailyIncome = dailyIncome,
            riskTolerance = riskTolerance,
            preferredSectors = preferredSectors.map { it.name },
            tradingFrequency = tradingFrequency.name
        )

        institutionEventPublishPort.publishInstitutionCreated(event)

        log.info(
            "기관투자자 생성 - 이름: {}, 유형: {}, 투자성향: {}, 자본금: {}원, 일일수입: {}원, " +
                "위험허용도: {}, 선호섹터: {}, 거래빈도: {}",
            institutionName, institutionType.displayName, investmentStyle.displayName,
            capital, dailyIncome, riskTolerance,
            preferredSectors.map { it.displayName }, tradingFrequency.displayName
        )
    }

    private fun generateRiskTolerance(style: InvestmentStyle): Double {
        val raw = when (style) {
            InvestmentStyle.AGGRESSIVE -> Random.nextDouble(0.7, 0.95)
            InvestmentStyle.STABLE -> Random.nextDouble(0.1, 0.4)
            InvestmentStyle.VALUE -> Random.nextDouble(0.3, 0.6)
            else -> Random.nextDouble(0.3, 0.6)
        }
        return (raw * 100.0).roundToInt() / 100.0
    }

    private fun generatePreferredSectors(): List<Sector> {
        val sectorCount = Random.nextInt(1, 4) // 1~3개
        return Sector.entries.shuffled().take(sectorCount)
    }

    private fun determineTradingFrequency(style: InvestmentStyle): TradingFrequency {
        return when (style) {
            InvestmentStyle.AGGRESSIVE -> TradingFrequency.HIGH
            InvestmentStyle.STABLE -> TradingFrequency.LOW
            InvestmentStyle.VALUE -> TradingFrequency.MEDIUM
            else -> TradingFrequency.MEDIUM
        }
    }
}
