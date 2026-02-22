package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 기관투자자 자동매매 트리거 스케줄러 (Inbound Adapter)
 * - HIGH: 5분마다 (공격형)
 * - MEDIUM: 15분마다 (가치투자형)
 * - LOW: 30분마다 (안정형)
 */
@Component
class InstitutionTradingScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 300_000)
    fun triggerHighFrequencyTrading() {
        log.info("기관투자자 자동매매 트리거 실행 - 빈도: HIGH")
        triggerPublishPort.publishInstitutionTradingTrigger("HIGH", 3)
    }

    @Scheduled(fixedRate = 900_000)
    fun triggerMediumFrequencyTrading() {
        log.info("기관투자자 자동매매 트리거 실행 - 빈도: MEDIUM")
        triggerPublishPort.publishInstitutionTradingTrigger("MEDIUM", 3)
    }

    @Scheduled(fixedRate = 1_800_000)
    fun triggerLowFrequencyTrading() {
        log.info("기관투자자 자동매매 트리거 실행 - 빈도: LOW")
        triggerPublishPort.publishInstitutionTradingTrigger("LOW", 3)
    }
}
