package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.npc.NpcNameQueryPort
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.StockServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NpcNameQueryAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : NpcNameQueryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getAllNpcNames(): List<String> {
        return try {
            stockServiceFeignClient.getNpcNames().data ?: emptyList()
        } catch (e: Exception) {
            log.warn("stock-service NPC 이름 목록 조회 실패: {}", e.message)
            emptyList()
        }
    }
}
