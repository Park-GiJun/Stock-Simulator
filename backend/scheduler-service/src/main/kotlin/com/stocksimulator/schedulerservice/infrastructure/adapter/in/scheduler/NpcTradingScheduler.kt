package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * NPC 자동매매 트리거 스케줄러 (Inbound Adapter)
 * - HIGH: 5분마다 (공격형)
 * - MEDIUM: 15분마다 (가치투자형)
 * - LOW: 30분마다 (안정형)
 */
@Component
class NpcTradingScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 300_000)
    fun triggerHighFrequencyTrading() {
        log.info("NPC 자동매매 트리거 실행 - 빈도: HIGH")
        triggerPublishPort.publishNpcTradingTrigger("HIGH", 5)
    }

    @Scheduled(fixedRate = 900_000)
    fun triggerMediumFrequencyTrading() {
        log.info("NPC 자동매매 트리거 실행 - 빈도: MEDIUM")
        triggerPublishPort.publishNpcTradingTrigger("MEDIUM", 5)
    }

    @Scheduled(fixedRate = 1_800_000)
    fun triggerLowFrequencyTrading() {
        log.info("NPC 자동매매 트리거 실행 - 빈도: LOW")
        triggerPublishPort.publishNpcTradingTrigger("LOW", 5)
    }
}
