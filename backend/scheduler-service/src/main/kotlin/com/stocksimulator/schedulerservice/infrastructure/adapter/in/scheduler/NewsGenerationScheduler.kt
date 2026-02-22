package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 뉴스 자동 생성 스케줄러 (Inbound Adapter)
 * - 5분마다 실행
 * - COMPANY (기업): 60% 확률
 * - INDUSTRY (산업): 20% 확률
 * - SOCIETY (사회): 5% 확률
 * - 각 레벨 독립 판정 → 여러 레벨 동시 발생 가능
 */
@Component
class NewsGenerationScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private data class LevelConfig(val level: String, val probability: Double)

    private val levels = listOf(
        LevelConfig("COMPANY", 0.6),
        LevelConfig("INDUSTRY", 0.2),
        LevelConfig("SOCIETY", 0.05)
    )

    @Scheduled(fixedRate = 300_000)
    fun generateNews() {
        log.info("뉴스 생성 스케줄러 실행")

        for (config in levels) {
            val roll = Random.nextDouble()
            if (roll < config.probability) {
                log.info("뉴스 생성 트리거: level={}, roll={} (기준: {})", config.level, "%.2f".format(roll), config.probability)
                triggerPublishPort.publishNewsGenerationTrigger(config.level)
            } else {
                log.debug("뉴스 생성 스킵: level={}, roll={} (기준: {})", config.level, "%.2f".format(roll), config.probability)
            }
        }
    }
}
