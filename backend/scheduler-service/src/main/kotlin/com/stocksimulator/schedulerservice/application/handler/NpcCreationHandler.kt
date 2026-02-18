package com.stocksimulator.schedulerservice.application.handler

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.common.event.NpcCreatedEvent
import com.stocksimulator.schedulerservice.application.port.out.npc.NpcEventPublishPort
import com.stocksimulator.schedulerservice.application.port.out.npc.NpcNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.roundToInt
import kotlin.random.Random

@Service
class NpcCreationHandler(
    private val npcEventPublishPort: NpcEventPublishPort,
    private val npcNameGeneratePort: NpcNameGeneratePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 개인 투자자(NPC) 생성
     * - 투자 성향: AGGRESSIVE, STABLE, VALUE, RANDOM (4가지 모두)
     * - 자본금: 20만 ~ 1억 KRW
     * - 주간 수입: 자본금의 5%
     * - 위험 허용도: 투자 성향에 따라 결정
     * - 선호 섹터: 1~3개 랜덤
     * - 거래 빈도: 투자 성향에 따라 결정
     */
    suspend fun createNpc() {
        val investmentStyle = InvestmentStyle.entries.toTypedArray().random()

        val capital = Random.nextLong(200_000L, 100_000_001L)
        val weeklyIncome = capital * 5 / 100

        val riskTolerance = generateRiskTolerance(investmentStyle)
        val preferredSectors = generatePreferredSectors()
        val tradingFrequency = determineTradingFrequency(investmentStyle)

        val npcName = npcNameGeneratePort.generate()

        val event = NpcCreatedEvent(
            npcName = npcName,
            investmentStyle = investmentStyle.name,
            capital = capital,
            weeklyIncome = weeklyIncome,
            riskTolerance = riskTolerance,
            preferredSectors = preferredSectors.map { it.name },
            tradingFrequency = tradingFrequency.name
        )

        npcEventPublishPort.publishNpcCreated(event)

        log.info(
            "개인투자자(NPC) 생성 - 이름: {}, 투자성향: {}, 자본금: {}원, 주간수입: {}원, " +
                "위험허용도: {}, 선호섹터: {}, 거래빈도: {}",
            npcName, investmentStyle.displayName,
            capital, weeklyIncome, riskTolerance,
            preferredSectors.map { it.displayName }, tradingFrequency.displayName
        )
    }

    private fun generateRiskTolerance(style: InvestmentStyle): Double {
        val raw = when (style) {
            InvestmentStyle.AGGRESSIVE -> Random.nextDouble(0.7, 0.95)
            InvestmentStyle.STABLE -> Random.nextDouble(0.1, 0.4)
            InvestmentStyle.VALUE -> Random.nextDouble(0.3, 0.6)
            InvestmentStyle.RANDOM -> Random.nextDouble(0.2, 0.8)
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
            InvestmentStyle.RANDOM -> TradingFrequency.entries.toTypedArray().random()
        }
    }
}
