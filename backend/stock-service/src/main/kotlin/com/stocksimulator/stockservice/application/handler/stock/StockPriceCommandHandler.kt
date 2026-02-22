package com.stocksimulator.stockservice.application.handler.stock

import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.common.util.PriceUtil
import com.stocksimulator.stockservice.application.dto.command.stock.UpdateStockPriceCommand
import com.stocksimulator.stockservice.application.port.`in`.stock.UpdateStockPriceUseCase
import com.stocksimulator.stockservice.application.port.out.stock.StockEventPublishPort
import com.stocksimulator.stockservice.application.port.out.stock.StockPersistencePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockPriceCommandHandler(
    private val stockPersistencePort: StockPersistencePort,
    private val stockEventPublishPort: StockEventPublishPort
) : UpdateStockPriceUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun updateStockPrice(command: UpdateStockPriceCommand) {
        val stock = stockPersistencePort.findById(command.stockId)
            ?: throw ResourceNotFoundException(ErrorCode.STOCK_NOT_FOUND)

        val previousPrice = stock.currentPrice
        val isBuy = command.matchedPrice >= previousPrice

        val tradeAmount = command.matchedPrice * command.matchedQuantity
        val marketCap = stock.currentPrice * stock.totalShares

        val priceImpact = PriceUtil.calculatePriceImpact(
            tradeAmount = tradeAmount,
            marketCap = marketCap,
            volatility = stock.volatility.toDouble(),
            isBuy = isBuy
        )

        val rawNewPrice = PriceUtil.applyPriceChange(stock.currentPrice, priceImpact)

        val (lowerLimit, upperLimit) = PriceUtil.getPriceLimits(stock.previousClose)
        val clampedPrice = rawNewPrice.coerceIn(lowerLimit, upperLimit)

        val newVolume = stock.volume + command.matchedQuantity
        val newHigh = maxOf(stock.high, clampedPrice)
        val newLow = if (stock.low == 0L) clampedPrice else minOf(stock.low, clampedPrice)

        val updatedStock = stock.updatePrice(
            newPrice = clampedPrice,
            newVolume = newVolume,
            newHigh = newHigh,
            newLow = newLow
        )

        stockPersistencePort.update(updatedStock)

        stockEventPublishPort.publishPriceUpdated(updatedStock, previousPrice, command.matchedQuantity)

        log.info(
            "주가 업데이트: stockId={}, {} → {} (변동률={:.2f}%, 거래량={})",
            command.stockId,
            previousPrice,
            clampedPrice,
            PriceUtil.calculateChangeRate(previousPrice, clampedPrice),
            newVolume
        )
    }
}
