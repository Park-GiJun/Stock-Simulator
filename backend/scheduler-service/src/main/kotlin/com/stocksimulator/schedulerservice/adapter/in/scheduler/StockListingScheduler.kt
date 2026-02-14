package com.stocksimulator.schedulerservice.adapter.`in`.scheduler

import com.stocksimulator.schedulerservice.application.handler.StockListingHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 주식 상장/상장폐지 스케줄러 (Inbound Adapter)
 * - IPO (신규 상장): 30분마다 40% 확률로 발생
 * - 상장폐지: 1시간마다 10% 확률로 발생
 */
@Component
class StockListingScheduler(
    private val stockListingHandler: StockListingHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 1_800_000)
    fun checkForIPO() {
        if (Random.nextDouble() < 0.4) {
            runBlocking { stockListingHandler.initiateIPO() }
        }
    }

    @Scheduled(fixedRate = 3_600_000)
    fun checkForDelisting() {
        if (Random.nextDouble() < 0.1) {
            stockListingHandler.initiateDelisting()
        }
    }
}
