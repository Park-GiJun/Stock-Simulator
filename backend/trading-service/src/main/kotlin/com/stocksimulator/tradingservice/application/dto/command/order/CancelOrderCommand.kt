package com.stocksimulator.tradingservice.application.dto.command.order

data class CancelOrderCommand(
    val orderId: String,
    val userId: String
)
