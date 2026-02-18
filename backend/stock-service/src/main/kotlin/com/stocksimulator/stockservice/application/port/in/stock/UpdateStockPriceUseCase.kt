package com.stocksimulator.stockservice.application.port.`in`.stock

import com.stocksimulator.stockservice.application.dto.command.stock.UpdateStockPriceCommand

interface UpdateStockPriceUseCase {
    fun updateStockPrice(command: UpdateStockPriceCommand)
}
