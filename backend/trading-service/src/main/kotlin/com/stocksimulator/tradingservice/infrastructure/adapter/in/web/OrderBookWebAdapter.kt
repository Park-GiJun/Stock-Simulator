package com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.toCreatedResponseEntity
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.tradingservice.application.dto.command.order.CancelOrderCommand
import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.dto.query.order.OrderBookQuery
import com.stocksimulator.tradingservice.application.port.`in`.order.CancelOrderUseCase
import com.stocksimulator.tradingservice.application.port.`in`.order.GetOrderBookUseCase
import com.stocksimulator.tradingservice.application.port.`in`.order.PlaceOrderUseCase
import com.stocksimulator.tradingservice.application.port.`in`.portfolio.GetTradeHistoryUseCase
import com.stocksimulator.tradingservice.application.port.out.order.OrderPersistencePort
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.OrderBookResponse
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.OrderHistoryResponse
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.PlaceOrderRequest
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.PlaceOrderResponse
import com.stocksimulator.tradingservice.infrastructure.adapter.`in`.web.dto.TradeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/trading")
@Tag(name = "Trading", description = "트레이딩 API")
class OrderBookWebAdapter(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val getOrderBookUseCase: GetOrderBookUseCase,
    private val getTradeHistoryUseCase: GetTradeHistoryUseCase,
    private val orderPersistencePort: OrderPersistencePort
) {
    @PostMapping("/orders")
    @Operation(summary = "주문 접수")
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest
    ): Mono<ResponseEntity<ApiResponse<PlaceOrderResponse>>> = mono {
        val command = PlaceOrderCommand(
            userId = request.userId,
            stockId = request.stockId,
            orderType = request.orderType,
            orderKind = request.orderKind,
            price = request.price,
            quantity = request.quantity,
            investorType = com.stocksimulator.common.dto.TradingInvestorType.USER
        )
        val result = placeOrderUseCase.placeOrder(command)
        ApiResponse.created(PlaceOrderResponse.from(result)).toCreatedResponseEntity()
    }

    @DeleteMapping("/orders/{orderId}")
    @Operation(summary = "주문 취소")
    fun cancelOrder(
        @Parameter(description = "주문 ID") @PathVariable orderId: String,
        @Parameter(description = "사용자 ID") @RequestParam userId: String
    ): Mono<ResponseEntity<ApiResponse<Unit>>> = mono {
        val command = CancelOrderCommand(orderId = orderId, userId = userId)
        cancelOrderUseCase.cancelOrder(command)
        ApiResponse.success<Unit>("주문이 취소되었습니다").toResponseEntity()
    }

    @GetMapping("/trades/stock/{stockId}")
    @Operation(summary = "종목별 체결 내역 조회")
    fun getStockTrades(
        @Parameter(description = "종목 ID") @PathVariable stockId: String
    ): Mono<ResponseEntity<ApiResponse<List<TradeResponse>>>> = mono {
        val results = getTradeHistoryUseCase.getTradesByStock(stockId)
        ApiResponse.success(results.map { TradeResponse.from(it) }).toResponseEntity()
    }

    @GetMapping("/orders/stock/{stockId}")
    @Operation(summary = "종목별 주문 내역 조회 (REJECTED 포함)")
    fun getStockOrders(
        @Parameter(description = "종목 ID") @PathVariable stockId: String
    ): Mono<ResponseEntity<ApiResponse<List<OrderHistoryResponse>>>> = mono {
        val results = orderPersistencePort.findByStockId(stockId)
        ApiResponse.success(results.map { OrderHistoryResponse.from(it) }).toResponseEntity()
    }

    @GetMapping("/order-book/{stockId}")
    @Operation(summary = "호가창 조회")
    fun getOrderBook(
        @Parameter(description = "종목 ID") @PathVariable stockId: String,
        @Parameter(description = "호가 깊이") @RequestParam(defaultValue = "10") depth: Int
    ): Mono<ResponseEntity<ApiResponse<OrderBookResponse>>> = mono {
        val query = OrderBookQuery(stockId = stockId, depth = depth)
        val result = getOrderBookUseCase.getOrderBook(query)
        ApiResponse.success(OrderBookResponse.from(result)).toResponseEntity()
    }
}
