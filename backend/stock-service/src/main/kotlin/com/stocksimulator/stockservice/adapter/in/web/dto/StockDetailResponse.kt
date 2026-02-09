package com.stocksimulator.stockservice.adapter.`in`.web.dto

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.StockStatus
import com.stocksimulator.stockservice.application.dto.result.stock.StockDetailResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "종목 상세 정보")
data class StockDetailResponse(
    @Schema(description = "종목 ID", example = "STK001")
    val stockId: String,
    @Schema(description = "종목명", example = "테크놀로지")
    val stockName: String,
    @Schema(description = "섹터", example = "IT")
    val sector: Sector,
    @Schema(description = "기준가", example = "50000")
    val basePrice: Long,
    @Schema(description = "현재가", example = "52300")
    val currentPrice: Long,
    @Schema(description = "전일 종가", example = "50000")
    val previousClose: Long,
    @Schema(description = "전일 대비 변동", example = "2300")
    val change: Long,
    @Schema(description = "전일 대비 변동률(%)", example = "4.6")
    val changePercent: Double,
    @Schema(description = "총 발행 주식수", example = "10000000")
    val totalShares: Long,
    @Schema(description = "시가총액 등급", example = "LARGE")
    val marketCapGrade: MarketCapGrade,
    @Schema(description = "변동성", example = "15")
    val volatility: Long,
    @Schema(description = "PER", example = "25.5")
    val per: Double,
    @Schema(description = "배당률", example = "0.01")
    val dividendRate: Double,
    @Schema(description = "성장률", example = "0.15")
    val growthRate: Double,
    @Schema(description = "이벤트 민감도", example = "1.2")
    val eventSensitivity: Double,
    @Schema(description = "거래량", example = "1250000")
    val volume: Long,
    @Schema(description = "고가", example = "53000")
    val high: Long,
    @Schema(description = "저가", example = "49500")
    val low: Long,
    @Schema(description = "시가", example = "50200")
    val open: Long,
    @Schema(description = "종목 상태", example = "LISTED")
    val status: StockStatus
) {
    companion object {
        fun from(result: StockDetailResult): StockDetailResponse = StockDetailResponse(
            stockId = result.stockId,
            stockName = result.stockName,
            sector = result.sector,
            basePrice = result.basePrice,
            currentPrice = result.currentPrice,
            previousClose = result.previousClose,
            change = result.change,
            changePercent = result.changePercent,
            totalShares = result.totalShares,
            marketCapGrade = result.marketCapGrade,
            volatility = result.volatility,
            per = result.per,
            dividendRate = result.dividendRate,
            growthRate = result.growthRate,
            eventSensitivity = result.eventSensitivity,
            volume = result.volume,
            high = result.high,
            low = result.low,
            open = result.open,
            status = result.status
        )
    }
}
