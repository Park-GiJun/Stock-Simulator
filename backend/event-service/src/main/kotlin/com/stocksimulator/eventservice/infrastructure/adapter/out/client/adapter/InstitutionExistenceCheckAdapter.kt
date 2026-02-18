package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.institution.InstitutionExistenceCheckPort
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.StockServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InstitutionExistenceCheckAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : InstitutionExistenceCheckPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun existsByName(name: String): Boolean {
        return try {
            stockServiceFeignClient.checkInstitutionExists(name).data ?: false
        } catch (e: Exception) {
            log.warn("stock-service 기관 이름 존재 확인 실패 (name={}): {}", name, e.message)
            false
        }
    }
}
