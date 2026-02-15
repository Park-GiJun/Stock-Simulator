package com.stocksimulator.stockservice.infrastructure.adapter.`in`.web.dto

import com.stocksimulator.common.dto.MarketCapGrade
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.stockservice.application.dto.result.stock.StockListItemResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "종목 목록 항목")
data class StockListItemResponse(
    @Schema(description = "종목 ID", example = "STK001")
    val stockId: String,
    @Schema(description = "종목명", example = "테크놀로지")
    val stockName: String,
    @Schema(description = "섹터", example = "IT")
    val sector: Sector,
    @Schema(description = "현재가", example = "52300")
    val currentPrice: Long,
    @Schema(description = "전일 대비 변동", example = "2300")
    val change: Long,
    @Schema(description = "전일 대비 변동률(%)", example = "4.6")
    val changePercent: Double,
    @Schema(description = "거래량", example = "1250000")
    val volume: Long,
    @Schema(description = "시가총액 등급", example = "LARGE")
    val marketCapGrade: MarketCapGrade
) {
    companion object {
        fun from(result: StockListItemResult): StockListItemResponse = StockListItemResponse(
            stockId = result.stockId,
            stockName = result.stockName,
            sector = result.sector,
            currentPrice = result.currentPrice,
            change = result.change,
            changePercent = result.changePercent,
            volume = result.volume,
            marketCapGrade = result.marketCapGrade
        )
    }
}
