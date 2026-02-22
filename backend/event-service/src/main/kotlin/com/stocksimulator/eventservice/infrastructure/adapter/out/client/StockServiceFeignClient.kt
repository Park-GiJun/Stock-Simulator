package com.stocksimulator.eventservice.infrastructure.adapter.out.client

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.InstitutionProfileResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.NpcProfileResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.RandomStockResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.StockExistsResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.StockListResponse
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

    @GetMapping("/api/investors/npcs/by-frequency")
    fun getNpcsByFrequency(
        @RequestParam frequency: String,
        @RequestParam maxCount: Int
    ): ApiResponse<List<NpcProfileResponse>>

    @GetMapping("/api/investors/institutions/by-frequency")
    fun getInstitutionsByFrequency(
        @RequestParam frequency: String,
        @RequestParam maxCount: Int
    ): ApiResponse<List<InstitutionProfileResponse>>

    @GetMapping("/api/investors/npcs/all")
    fun getAllNpcs(): ApiResponse<List<NpcProfileResponse>>

    @GetMapping("/api/investors/institutions/all")
    fun getAllInstitutions(): ApiResponse<List<InstitutionProfileResponse>>

    @GetMapping("/api/stocks")
    fun getStocks(
        @RequestParam(required = false) page: Int? = 0,
        @RequestParam(required = false) size: Int? = 20,
        @RequestParam(required = false) sector: String? = null
    ): ApiResponse<com.stocksimulator.common.dto.PageResponse<StockListResponse>>
}
