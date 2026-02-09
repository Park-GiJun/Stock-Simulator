package com.stocksimulator.stockservice.adapter.out.persistence.stock.entity

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.stockservice.domain.StockModel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "stocks",
    schema = "stocks",
    indexes = [
        Index(name = "idx_stocks_sector", columnList = "sector"),
        Index(name = "idx_stocks_status", columnList = "status"),
        Index(name = "idx_stocks_market_cap_grade", columnList = "market_cap_grade"),
        Index(name = "idx_stocks_stock_name", columnList = "stock_name")
    ]
)
class StockJpaEntity(
    @Id
    @Column(name = "stock_id", length = 20)
    val stockId: String,

    @Column(name = "stock_name", nullable = false, length = 100)
    var stockName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val sector: Sector,

    @Column(name = "base_price", nullable = false)
    val basePrice: Long,

    @Column(name = "current_price", nullable = false)
    var currentPrice: Long,

    @Column(name = "previous_close", nullable = false)
    var previousClose: Long,

    @Column(name = "total_shares", nullable = false)
    val totalShares: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "market_cap_grade", nullable = false, length = 20)
    val marketCapGrade: MarketCapGrade,

    @Column(nullable = false)
    var volatility: Long = 0L,

    @Column(nullable = false)
    var per: Double = 0.0,

    @Column(name = "dividend_rate", nullable = false)
    var dividendRate: Double = 0.0,

    @Column(name = "growth_rate", nullable = false)
    var growthRate: Double = 0.0,

    @Column(name = "event_sensitivity", nullable = false)
    var eventSensitivity: Double = 0.0,

    @Column(nullable = false)
    var volume: Long = 0L,

    @Column(nullable = false)
    var high: Long = 0L,

    @Column(nullable = false)
    var low: Long = 0L,

    @Column(name = "open_price", nullable = false)
    var openPrice: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StockStatus = StockStatus.LISTED,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): StockModel = StockModel.of(
        stockId = stockId,
        stockName = stockName,
        sector = sector,
        basePrice = basePrice,
        currentPrice = currentPrice,
        previousClose = previousClose,
        totalShares = totalShares,
        marketCapGrade = marketCapGrade,
        volatility = volatility,
        per = per,
        dividendRate = dividendRate,
        growthRate = growthRate,
        eventSensitivity = eventSensitivity,
        volume = volume,
        high = high,
        low = low,
        open = openPrice,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFromDomain(domain: StockModel) {
        currentPrice = domain.currentPrice
        previousClose = domain.previousClose
        volatility = domain.volatility
        per = domain.per
        dividendRate = domain.dividendRate
        growthRate = domain.growthRate
        eventSensitivity = domain.eventSensitivity
        volume = domain.volume
        high = domain.high
        low = domain.low
        openPrice = domain.open
        status = domain.status
        updatedAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StockJpaEntity) return false
        return stockId == other.stockId
    }

    override fun hashCode(): Int = stockId.hashCode()

    companion object {
        fun fromDomain(domain: StockModel): StockJpaEntity = StockJpaEntity(
            stockId = domain.stockId,
            stockName = domain.stockName,
            sector = domain.sector,
            basePrice = domain.basePrice,
            currentPrice = domain.currentPrice,
            previousClose = domain.previousClose,
            totalShares = domain.totalShares,
            marketCapGrade = domain.marketCapGrade,
            volatility = domain.volatility,
            per = domain.per,
            dividendRate = domain.dividendRate,
            growthRate = domain.growthRate,
            eventSensitivity = domain.eventSensitivity,
            volume = domain.volume,
            high = domain.high,
            low = domain.low,
            openPrice = domain.open,
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
