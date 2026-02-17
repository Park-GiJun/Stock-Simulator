package com.stocksimulator.tradingservice.application.dto.command.order

import com.stocksimulator.common.dto.OrderKind
import com.stocksimulator.common.dto.OrderType

data class PlaceOrderCommand(
    val userId: String,
    val stockId: String,
    val orderType: OrderType,
    val orderKind: OrderKind,
    val price: Long?,
    val quantity: Long
)
