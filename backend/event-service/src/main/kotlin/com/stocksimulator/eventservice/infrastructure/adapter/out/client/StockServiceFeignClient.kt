package com.stocksimulator.eventservice.infrastructure.adapter.out.client

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.RandomStockResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.StockExistsResponse
import org.springframework.cloud.openfeign.FeignClient
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

    @GetMapping("/api/investors/npcs/names")
    fun getNpcNames(): ApiResponse<List<String>>

    @GetMapping("/api/investors/institutions/exists")
    fun checkInstitutionExists(@RequestParam name: String): ApiResponse<Boolean>
}
