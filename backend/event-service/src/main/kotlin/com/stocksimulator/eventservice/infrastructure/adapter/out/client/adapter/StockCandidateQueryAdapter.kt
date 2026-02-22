package com.stocksimulator.eventservice.infrastructure.adapter.out.client.adapter

import com.stocksimulator.eventservice.application.port.out.trading.StockCandidateDto
import com.stocksimulator.eventservice.application.port.out.trading.StockCandidateQueryPort
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.StockServiceFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class StockCandidateQueryAdapter(
    private val stockServiceFeignClient: StockServiceFeignClient
) : StockCandidateQueryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getStocksBySector(sector: String, maxCount: Int): List<StockCandidateDto> {
        return try {
            val response = stockServiceFeignClient.getStocks(page = 0, size = maxCount, sector = sector)
            response.data?.content?.map { stock ->
                StockCandidateDto(
                    stockId = stock.stockId,
                    stockName = stock.stockName,
                    sector = stock.sector,
                    currentPrice = stock.currentPrice,
                    changePercent = stock.changePercent
                )
            } ?: emptyList()
        } catch (e: Exception) {
            log.warn("후보 종목 조회 실패: sector={}, error={}", sector, e.message)
            emptyList()
        }
    }
}
