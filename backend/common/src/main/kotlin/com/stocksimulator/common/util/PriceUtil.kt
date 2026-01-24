package com.stocksimulator.common.util

/**
 * 호가 단위 계산 유틸리티
 *
 * | 주가 범위       | 호가 단위 |
 * |----------------|----------|
 * | ~1,000원       | 1원      |
 * | ~5,000원       | 5원      |
 * | ~10,000원      | 10원     |
 * | ~50,000원      | 50원     |
 * | ~100,000원     | 100원    |
 * | ~500,000원     | 500원    |
 * | 500,000원~     | 1,000원  |
 */
object PriceUtil {

    /**
     * 주가에 따른 호가 단위 반환
     */
    fun getTickSize(price: Long): Long {
        return when {
            price <= 1_000 -> 1
            price <= 5_000 -> 5
            price <= 10_000 -> 10
            price <= 50_000 -> 50
            price <= 100_000 -> 100
            price <= 500_000 -> 500
            else -> 1_000
        }
    }

    /**
     * 가격을 호가 단위에 맞게 조정 (올림)
     */
    fun adjustPriceUp(price: Long): Long {
        val tickSize = getTickSize(price)
        return ((price + tickSize - 1) / tickSize) * tickSize
    }

    /**
     * 가격을 호가 단위에 맞게 조정 (내림)
     */
    fun adjustPriceDown(price: Long): Long {
        val tickSize = getTickSize(price)
        return (price / tickSize) * tickSize
    }

    /**
     * 가격이 유효한 호가 단위인지 확인
     */
    fun isValidPrice(price: Long): Boolean {
        if (price <= 0) return false
        val tickSize = getTickSize(price)
        return price % tickSize == 0L
    }

    /**
     * 주가 변동률 계산
     */
    fun calculateChangeRate(previousPrice: Long, currentPrice: Long): Double {
        if (previousPrice == 0L) return 0.0
        return ((currentPrice - previousPrice).toDouble() / previousPrice) * 100
    }

    /**
     * 체결에 의한 주가 변동 계산
     *
     * 변동률 = (체결금액 / 시가총액) × volatility × direction
     */
    fun calculatePriceImpact(
        tradeAmount: Long,          // 체결금액 = 체결가 × 체결수량
        marketCap: Long,            // 시가총액 = 현재가 × 발행주식수
        volatility: Double,         // 종목별 변동성 (0.1 ~ 2.0)
        isBuy: Boolean              // 매수: true, 매도: false
    ): Double {
        if (marketCap == 0L) return 0.0
        val baseImpact = tradeAmount.toDouble() / marketCap
        val direction = if (isBuy) 1.0 else -1.0
        return baseImpact * volatility * direction * 100 // 퍼센트로 반환
    }

    /**
     * 이벤트에 의한 주가 변동 계산
     *
     * 변동률 = intensity × eventSensitivity × sentimentMultiplier
     */
    fun calculateEventImpact(
        intensity: Double,          // 이벤트 강도 (0.1 ~ 2.0)
        eventSensitivity: Double,   // 종목별 이벤트 민감도 (0.5 ~ 2.0)
        sentimentMultiplier: Int    // POSITIVE=+1, NEGATIVE=-1, NEUTRAL=0
    ): Double {
        return intensity * eventSensitivity * sentimentMultiplier * 100 // 퍼센트로 반환
    }

    /**
     * 새로운 가격 계산 (변동률 적용)
     */
    fun applyPriceChange(currentPrice: Long, changeRatePercent: Double): Long {
        val newPrice = currentPrice * (1 + changeRatePercent / 100)
        val adjustedPrice = adjustPriceDown(newPrice.toLong())
        return maxOf(adjustedPrice, getTickSize(currentPrice)) // 최소 1틱 보장
    }

    /**
     * 가격 제한 범위 계산 (상한가/하한가)
     * 기본 30% 제한
     */
    fun getPriceLimits(basePrice: Long, limitPercent: Double = 30.0): Pair<Long, Long> {
        val upperLimit = adjustPriceUp((basePrice * (1 + limitPercent / 100)).toLong())
        val lowerLimit = adjustPriceDown((basePrice * (1 - limitPercent / 100)).toLong())
        return Pair(lowerLimit, upperLimit)
    }
}
