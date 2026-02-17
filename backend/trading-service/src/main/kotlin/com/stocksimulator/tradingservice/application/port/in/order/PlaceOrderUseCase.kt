package com.stocksimulator.tradingservice.application.port.`in`.order

import com.stocksimulator.tradingservice.application.dto.command.order.PlaceOrderCommand
import com.stocksimulator.tradingservice.application.dto.result.order.PlaceOrderResult

interface PlaceOrderUseCase {
    fun placeOrder(command: PlaceOrderCommand): PlaceOrderResult
}
