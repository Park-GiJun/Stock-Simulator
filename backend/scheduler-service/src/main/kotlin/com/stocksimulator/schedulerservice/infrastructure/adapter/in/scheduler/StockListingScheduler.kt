package com.stocksimulator.schedulerservice.infrastructure.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.port.out.TriggerPublishPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 주식 상장/상장폐지 스케줄러 (Inbound Adapter) - 비활성화됨
 * - IPO (신규 상장): 30분마다 40% 확률로 발생
 * - 상장폐지: 1시간마다 10% 확률로 발생
 */
@Component
class StockListingScheduler(
    private val triggerPublishPort: TriggerPublishPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // @Scheduled(fixedRate = 1_800_000) // 비활성화
    fun checkForIPO() {
        val roll = Random.nextDouble()
        log.info("IPO 스케줄러 실행 - 확률: {} (기준: 0.4)", "%.2f".format(roll))
        if (roll < 0.4) {
            triggerPublishPort.publishStockListingTrigger()
        } else {
            log.info("IPO 스킵 (확률 미달)")
        }
    }

    // @Scheduled(fixedRate = 3_600_000) // 비활성화
    fun checkForDelisting() {
        val roll = Random.nextDouble()
        log.info("상장폐지 스케줄러 실행 - 확률: {} (기준: 0.1)", "%.2f".format(roll))
        if (roll < 0.1) {
            triggerPublishPort.publishStockDelistingTrigger()
        } else {
            log.info("상장폐지 스킵 (확률 미달)")
        }
    }
}
