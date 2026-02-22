package com.stocksimulator.eventservice.infrastructure.adapter.out.client

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.BalanceResponse
import com.stocksimulator.eventservice.infrastructure.adapter.out.client.dto.PortfolioResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "trading-service")
interface TradingServiceFeignClient {

    @GetMapping("/api/trading/portfolio/{investorId}/balance")
    fun getBalance(
        @PathVariable investorId: String,
        @RequestParam investorType: String
    ): ApiResponse<BalanceResponse>

    @GetMapping("/api/trading/portfolio/{investorId}")
    fun getPortfolio(
        @PathVariable investorId: String,
        @RequestParam investorType: String
    ): ApiResponse<PortfolioResponse>
}
