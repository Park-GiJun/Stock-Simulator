package com.stocksimulator.schedulerservice.infrastructure.adapter.out.client

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.schedulerservice.domain.RandomStockInfo
import com.stocksimulator.schedulerservice.application.port.out.stock.StockExistenceCheckPort
import com.stocksimulator.schedulerservice.application.port.out.stock.StockQueryPort
import com.stocksimulator.schedulerservice.infrastructure.adapter.out.client.dto.RandomStockResponse
import com.stocksimulator.schedulerservice.infrastructure.adapter.out.client.dto.StockExistsResponse
import org.slf4j.LoggerFactory
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "stock-service")
interface StockServiceFeignClient {

    @GetMapping("/api/stocks/exists")
    fun checkStockExists(
        @RequestParam(required = false) stockId: String? = null,
        @RequestParam(required = false) stockName: String? = null
    ): ApiResponse<StockExistsResponse>

    @GetMapping("/api/stocks/random")
    fun getRandomStock(): ApiResponse<RandomStockResponse?>
}

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
