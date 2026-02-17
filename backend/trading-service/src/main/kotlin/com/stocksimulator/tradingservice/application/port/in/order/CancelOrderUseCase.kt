package com.stocksimulator.tradingservice.application.port.`in`.order

import com.stocksimulator.tradingservice.application.dto.command.order.CancelOrderCommand

interface CancelOrderUseCase {
    fun cancelOrder(command: CancelOrderCommand)
}
