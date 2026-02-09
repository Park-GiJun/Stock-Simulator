package com.stocksimulator.stockservice.application.port.`in`.stock

import com.stocksimulator.stockservice.application.dto.command.stock.CreateStockCommand
import com.stocksimulator.stockservice.application.dto.result.stock.StockDetailResult

interface CreateStockUseCase {
    fun createStock(command: CreateStockCommand): StockDetailResult
}
