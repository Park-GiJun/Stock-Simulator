package com.stocksimulator.eventservice.application.handler.institution

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.common.event.InstitutionCreatedEvent
import com.stocksimulator.eventservice.application.port.out.institution.InstitutionEventPublishPort
import com.stocksimulator.eventservice.application.port.out.institution.InstitutionExistenceCheckPort
import com.stocksimulator.eventservice.application.port.out.institution.InstitutionNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.roundToInt
import kotlin.random.Random

@Service
class InstitutionCreationHandler(
    private val institutionEventPublishPort: InstitutionEventPublishPort,
    private val institutionNameGeneratePort: InstitutionNameGeneratePort,
    private val institutionExistenceCheckPort: InstitutionExistenceCheckPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

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

        val institutionName = generateUniqueName(institutionType)

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

    private suspend fun generateUniqueName(institutionType: InstitutionType): String {
        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            val name = institutionNameGeneratePort.generate(institutionType)

            val exists = try {
                institutionExistenceCheckPort.existsByName(name)
            } catch (e: Exception) {
                log.warn("기관 이름 중복 확인 실패 (name={}): {}", name, e.message)
                false
            }

            if (!exists) {
                return name
            }
            log.info("기관 이름 중복 발견: {} (시도 {}회), 재생성 시도", name, attempt + 1)
        }

        val fallbackName = "${institutionNameGeneratePort.generate(institutionType)}${Random.nextInt(10, 100)}"
        log.warn("기관 이름 재시도 초과, fallback 이름 사용: {}", fallbackName)
        return fallbackName
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
        val sectorCount = Random.nextInt(1, 4)
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
