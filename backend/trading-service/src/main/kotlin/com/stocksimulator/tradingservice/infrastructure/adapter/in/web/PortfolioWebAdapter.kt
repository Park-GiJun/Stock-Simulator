package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.TradingInvestorType
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.tradingservice.application.dto.query.portfolio.InvestorBalanceQuery
import com.stocksimulator.tradingservice.application.dto.query.portfolio.PortfolioQuery
import com.stocksimulator.tradingservice.application.dto.query.portfolio.TradeHistoryQuery
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetInvestorBalanceUseCase
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetPortfolioUseCase
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetTradeHistoryUseCase
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.InvestorBalanceResponse
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.PortfolioResponse
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.TradeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/trading/portfolio")
@Tag(name = "Portfolio", description = "포트폴리오 API")
class PortfolioWebAdapter(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val getTradeHistoryUseCase: GetTradeHistoryUseCase,
    private val getInvestorBalanceUseCase: GetInvestorBalanceUseCase
) {
    @GetMapping("/{investorId}")
    @Operation(summary = "포트폴리오 조회")
    fun getPortfolio(
        @Parameter(description = "투자자 ID") @PathVariable investorId: String,
        @Parameter(description = "투자자 유형") @RequestParam(defaultValue = "USER") investorType: TradingInvestorType
    ): Mono<ResponseEntity<ApiResponse<PortfolioResponse>>> = mono {
        val query = PortfolioQuery(investorId = investorId, investorType = investorType)
        val results = getPortfolioUseCase.getPortfolio(query)
        ApiResponse.success(PortfolioResponse.from(results)).toResponseEntity()
    }

    @GetMapping("/{investorId}/trades")
    @Operation(summary = "거래내역 조회")
    fun getTradeHistory(
        @Parameter(description = "투자자 ID") @PathVariable investorId: String,
        @Parameter(description = "투자자 유형") @RequestParam(defaultValue = "USER") investorType: TradingInvestorType
    ): Mono<ResponseEntity<ApiResponse<List<TradeResponse>>>> = mono {
        val query = TradeHistoryQuery(investorId = investorId, investorType = investorType)
        val results = getTradeHistoryUseCase.getTradeHistory(query)
        ApiResponse.success(results.map { TradeResponse.from(it) }).toResponseEntity()
    }

    @GetMapping("/{investorId}/balance")
    @Operation(summary = "잔고 조회")
    fun getBalance(
        @Parameter(description = "투자자 ID") @PathVariable investorId: String,
        @Parameter(description = "투자자 유형") @RequestParam(defaultValue = "USER") investorType: TradingInvestorType
    ): Mono<ResponseEntity<ApiResponse<InvestorBalanceResponse>>> = mono {
        val query = InvestorBalanceQuery(investorId = investorId, investorType = investorType)
        val result = getInvestorBalanceUseCase.getBalance(query)
        ApiResponse.success(InvestorBalanceResponse.from(result)).toResponseEntity()
    }
}
