package com.stocksimulator.stockservice.application.dto.command.stock

import java.time.Instant

data class UpdateStockPriceCommand(
    val tradeId: String,
    val stockId: String,
    val matchedPrice: Long,
    val matchedQuantity: Long,
    val matchedAt: Instant
)
