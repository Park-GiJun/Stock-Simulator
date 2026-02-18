package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.stock.StockExistenceCheckPort
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.StockServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class StockExistenceCheckAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : StockExistenceCheckPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun existsByStockId(stockId: String): Boolean {
        return try {
            stockServiceFeignClient.checkStockExists(stockId = stockId).data?.stockIdExists ?: false
        } catch (e: Exception) {
            log.warn("stock-service 호출 실패 (stockId={}): {}", stockId, e.message)
            false
        }
    }

    override fun existsByStockName(stockName: String): Boolean {
        return try {
            stockServiceFeignClient.checkStockExists(stockName = stockName).data?.stockNameExists ?: false
        } catch (e: Exception) {
            log.warn("stock-service 호출 실패 (stockName={}): {}", stockName, e.message)
            false
        }
    }
}
