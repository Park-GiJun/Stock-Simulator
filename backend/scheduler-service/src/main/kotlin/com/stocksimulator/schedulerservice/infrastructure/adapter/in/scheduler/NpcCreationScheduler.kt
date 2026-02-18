package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 개인투자자(NPC) 생성 스케줄러 (Inbound Adapter)
 * - 10분마다 1~3명의 개인투자자 생성
 */
@Component
class NpcCreationScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 600_000)
    fun createNpcs() {
        val count = Random.nextInt(1, 4)
        log.info("개인투자자(NPC) 생성 스케줄러 실행 - 생성 수: {}", count)
        triggerPublishPort.publishNpcCreationTrigger(count)
    }
}
