package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 기관투자자 생성 스케줄러 (Inbound Adapter)
 * - 2시간마다 50% 확률로 기관투자자 1개 생성
 */
@Component
class InstitutionCreationScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 7_200_000)
    fun checkForInstitutionCreation() {
        val roll = Random.nextDouble()
        log.info("기관투자자 생성 스케줄러 실행 - 확률: {} (기준: 0.5)", "%.2f".format(roll))
        if (roll < 0.5) {
            triggerPublishPort.publishInstitutionCreationTrigger()
        } else {
            log.info("기관투자자 생성 스킵 (확률 미달)")
        }
    }
}
