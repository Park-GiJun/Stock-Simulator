package com.stocksimulator.stockservice.application.handler.stock

import com.stocksimulator.common.exception.DuplicateResourceException
import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.stockservice.application.dto.command.stock.CreateStockCommand
import com.stocksimulator.stockservice.application.dto.result.stock.StockDetailResult
import com.stocksimulator.stockservice.application.port.`in`.stock.CreateStockUseCase
import com.stocksimulator.stockservice.application.port.`in`.stock.DelistStockUseCase
import com.stocksimulator.stockservice.application.port.out.stock.StockPersistencePort
import com.stocksimulator.stockservice.domain.model.StockModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockCommandHandler(
    private val stockPersistencePort: StockPersistencePort
) : CreateStockUseCase, DelistStockUseCase {

    @Transactional
    override fun createStock(command: CreateStockCommand): StockDetailResult {
        stockPersistencePort.findById(command.stockId)?.let {
            throw DuplicateResourceException(
                ErrorCode.INVALID_STOCK_STATUS,
                "이미 존재하는 종목입니다: ${command.stockId}"
            )
        }

        val newStock = StockModel.create(
            stockId = command.stockId,
            stockName = command.stockName,
            sector = command.sector,
            basePrice = command.basePrice,
            totalShares = command.totalShares,
            marketCapGrade = command.marketCapGrade,
            volatility = command.volatility,
            per = command.per,
            dividendRate = command.dividendRate,
            growthRate = command.growthRate,
            eventSensitivity = command.eventSensitivity
        )

        val savedStock = stockPersistencePort.save(newStock)
        return StockDetailResult.from(savedStock)
    }

    @Transactional
    override fun delistStock(stockId: String) {
        val stock = stockPersistencePort.findById(stockId)
            ?: throw ResourceNotFoundException(ErrorCode.STOCK_NOT_FOUND)

        val delistedStock = stock.delist()
        stockPersistencePort.update(delistedStock)
    }
}
