package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.stock.StockQueryPort
import com.stocksimulator.eventservice.domain.RandomStockInfo
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.StockServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class StockQueryAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : StockQueryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getRandomListedStock(): RandomStockInfo? {
        return try {
            val response = stockServiceFeignClient.getRandomStock().data
            response?.let {
                RandomStockInfo(
                    stockId = it.stockId,
                    stockName = it.stockName,
                    currentPrice = it.currentPrice
                )
            }
        } catch (e: Exception) {
            log.warn("stock-service 랜덤 종목 조회 실패: {}", e.message)
            null
        }
    }
}
