package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * NPC 자동매매 트리거 스케줄러 (Inbound Adapter)
 * - 5분마다 전체 NPC 대상 매매 트리거 발행
 * - 개별 NPC의 tradingFrequency 기반 확률 판정은 event-service에서 수행
 */
@Component
class NpcTradingScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 300_000)
    fun triggerNpcTrading() {
        log.info("NPC 자동매매 트리거 실행")
        triggerPublishPort.publishNpcTradingTrigger()
    }
}
