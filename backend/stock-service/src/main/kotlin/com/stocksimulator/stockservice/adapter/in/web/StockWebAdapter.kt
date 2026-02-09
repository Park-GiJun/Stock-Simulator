package com.stocksimulator.stockservice.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.PageResponse
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.stockservice.adapter.`in`.web.dto.StockDetailResponse
import com.stocksimulator.stockservice.adapter.`in`.web.dto.StockListItemResponse
import com.stocksimulator.stockservice.application.dto.query.stock.StockListQuery
import com.stocksimulator.stockservice.application.port.`in`.stock.GetStockDetailUseCase
import com.stocksimulator.stockservice.application.port.`in`.stock.GetStockListUseCase
import com.stocksimulator.stockservice.application.port.`in`.stock.SearchStockUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock", description = "종목 API")
class StockWebAdapter(
    private val getStockListUseCase: GetStockListUseCase,
    private val getStockDetailUseCase: GetStockDetailUseCase,
    private val searchStockUseCase: SearchStockUseCase
) {

    @GetMapping
    @Operation(summary = "종목 목록 조회", description = "필터링, 정렬, 페이지네이션을 지원하는 종목 목록 조회")
    fun getStocks(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "섹터 필터") @RequestParam(required = false) sector: String?,
        @Parameter(description = "시가총액 등급 필터") @RequestParam(required = false) marketCap: String?,
        @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "stockName") sortBy: String,
        @Parameter(description = "정렬 순서") @RequestParam(defaultValue = "asc") sortOrder: String,
        @Parameter(description = "검색어") @RequestParam(required = false) search: String?
    ): Mono<ResponseEntity<ApiResponse<PageResponse<StockListItemResponse>>>> = mono {
        val query = StockListQuery(
            page = page,
            size = size,
            sector = sector,
            marketCap = marketCap,
            sortBy = sortBy,
            sortOrder = sortOrder,
            search = search
        )

        val result = getStockListUseCase.getStockList(query)
        val responseContent = result.content.map { StockListItemResponse.from(it) }
        val pageResponse = PageResponse.of(
            content = responseContent,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements
        )

        ApiResponse.success(pageResponse).toResponseEntity()
    }

    @GetMapping("/{stockId}")
    @Operation(summary = "종목 상세 조회", description = "종목 ID로 상세 정보 조회")
    fun getStockDetail(
        @Parameter(description = "종목 ID") @PathVariable stockId: String
    ): Mono<ResponseEntity<ApiResponse<StockDetailResponse>>> = mono {
        val result = getStockDetailUseCase.getStockDetail(stockId)
        val response = StockDetailResponse.from(result)
        ApiResponse.success(response).toResponseEntity()
    }

    @GetMapping("/search")
    @Operation(summary = "종목 검색", description = "종목명 또는 종목 ID로 검색")
    fun searchStocks(
        @Parameter(description = "검색어") @RequestParam q: String
    ): Mono<ResponseEntity<ApiResponse<List<StockListItemResponse>>>> = mono {
        val results = searchStockUseCase.searchStocks(q)
        val response = results.map { StockListItemResponse.from(it) }
        ApiResponse.success(response).toResponseEntity()
    }

    @GetMapping("/{stockId}/orderbook")
    @Operation(summary = "호가 조회", description = "종목의 현재 호가 정보 조회 (stub)")
    fun getOrderBook(
        @Parameter(description = "종목 ID") @PathVariable stockId: String
    ): Mono<ResponseEntity<ApiResponse<Map<String, Any>>>> = mono {
        val stub = mapOf(
            "stockId" to stockId,
            "asks" to emptyList<Any>(),
            "bids" to emptyList<Any>(),
            "timestamp" to System.currentTimeMillis().toString()
        )
        ApiResponse.success(stub).toResponseEntity()
    }

    @GetMapping("/{stockId}/candles")
    @Operation(summary = "캔들 차트 데이터 조회", description = "종목의 캔들 차트 데이터 조회 (stub)")
    fun getCandles(
        @Parameter(description = "종목 ID") @PathVariable stockId: String,
        @Parameter(description = "캔들 간격") @RequestParam(defaultValue = "1d") interval: String,
        @Parameter(description = "조회 개수") @RequestParam(defaultValue = "30") limit: Int
    ): Mono<ResponseEntity<ApiResponse<Map<String, Any>>>> = mono {
        val stub = mapOf(
            "stockId" to stockId,
            "interval" to interval,
            "candles" to emptyList<Any>()
        )
        ApiResponse.success(stub).toResponseEntity()
    }

    @GetMapping("/{stockId}/trades")
    @Operation(summary = "체결 내역 조회", description = "종목의 최근 체결 내역 조회 (stub)")
    fun getTrades(
        @Parameter(description = "종목 ID") @PathVariable stockId: String,
        @Parameter(description = "조회 개수") @RequestParam(defaultValue = "50") limit: Int
    ): Mono<ResponseEntity<ApiResponse<Map<String, Any>>>> = mono {
        val stub = mapOf(
            "stockId" to stockId,
            "trades" to emptyList<Any>()
        )
        ApiResponse.success(stub).toResponseEntity()
    }
}
