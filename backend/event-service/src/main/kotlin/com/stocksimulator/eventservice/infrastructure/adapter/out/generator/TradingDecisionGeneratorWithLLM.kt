package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import com.stocksimulator.eventservice.application.port.out.trading.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class TradingDecisionGeneratorWithLLM : TradingDecisionGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun generateDecision(request: TradingDecisionRequest): TradingDecisionResult {
        val allowedActions = request.allowedActions

        // 투자 성향별 행동 확률 결정
        val (buyProb, sellProb) = calculateActionProbabilities(request)

        val roll = Random.nextDouble()

        val action = when {
            "BUY" in allowedActions && "SELL" in allowedActions -> {
                when {
                    roll < buyProb -> "BUY"
                    roll < buyProb + sellProb -> "SELL"
                    else -> "HOLD"
                }
            }
            "BUY" in allowedActions -> {
                if (roll < buyProb) "BUY" else "HOLD"
            }
            "SELL" in allowedActions -> {
                if (roll < sellProb) "SELL" else "HOLD"
            }
            else -> "HOLD"
        }

        return when (action) {
            "BUY" -> generateBuyDecision(request)
            "SELL" -> generateSellDecision(request)
            else -> {
                log.debug("매매 결정: HOLD - investor={}", request.investorName)
                TradingDecisionResult(action = "HOLD", stockId = null, quantity = 0)
            }
        }
    }

    /**
     * 투자 성향 + 리스크 허용도 + 뉴스 감정 기반 매수/매도 확률 계산
     */
    private fun calculateActionProbabilities(request: TradingDecisionRequest): Pair<Double, Double> {
        // 기본 확률 (투자 성향별)
        var baseBuyProb = when (request.investmentStyle) {
            "AGGRESSIVE" -> 0.40
            "STABLE" -> 0.15
            "VALUE" -> 0.25
            else -> 0.20 // RANDOM
        }

        var baseSellProb = when (request.investmentStyle) {
            "AGGRESSIVE" -> 0.25
            "STABLE" -> 0.10
            "VALUE" -> 0.15
            else -> 0.20
        }

        // 리스크 허용도 보정 (높을수록 매수 확률 증가)
        baseBuyProb *= (0.7 + request.riskTolerance * 0.6)
        baseSellProb *= (1.3 - request.riskTolerance * 0.6)

        // 뉴스 감정 보정
        if (request.recentNews.isNotEmpty()) {
            val positiveCount = request.recentNews.count { it.sentiment == "POSITIVE" }
            val negativeCount = request.recentNews.count { it.sentiment == "NEGATIVE" }
            val newsTotal = request.recentNews.size.toDouble()

            val sentimentBias = (positiveCount - negativeCount) / newsTotal * 0.15
            baseBuyProb += sentimentBias
            baseSellProb -= sentimentBias
        }

        // 보유 종목이 많으면 매도 확률 증가, 적으면 매수 확률 증가
        if (request.currentHoldings.isEmpty()) {
            baseBuyProb += 0.10
            baseSellProb = 0.0
        } else if (request.currentHoldings.size >= 5) {
            baseSellProb += 0.10
            baseBuyProb -= 0.05
        }

        return Pair(baseBuyProb.coerceIn(0.0, 0.6), baseSellProb.coerceIn(0.0, 0.4))
    }

    private fun generateBuyDecision(request: TradingDecisionRequest): TradingDecisionResult {
        if (request.stockCandidates.isEmpty()) {
            return TradingDecisionResult(action = "HOLD", stockId = null, quantity = 0)
        }

        // 투자 성향별 최대 투자 비율
        val maxInvestmentRate = when (request.investmentStyle) {
            "AGGRESSIVE" -> 0.30
            "STABLE" -> 0.10
            "VALUE" -> 0.20
            else -> 0.15
        }
        val maxInvestment = (request.availableCash * maxInvestmentRate).toLong()
        if (maxInvestment <= 0) {
            return TradingDecisionResult(action = "HOLD", stockId = null, quantity = 0)
        }

        // 후보 종목 중 선호 섹터 우선, 없으면 전체 중 랜덤
        val preferredCandidates = request.stockCandidates.filter { it.sector in request.preferredSectors }
        val candidates = preferredCandidates.ifEmpty { request.stockCandidates }

        // 뉴스 감정 가중치 적용: 긍정 뉴스 관련 섹터 종목 우선
        val positiveNewsSectors = request.recentNews
            .filter { it.sentiment == "POSITIVE" && it.sector != null }
            .mapNotNull { it.sector }
            .toSet()

        val weightedCandidates = if (positiveNewsSectors.isNotEmpty()) {
            val boosted = candidates.filter { it.sector in positiveNewsSectors }
            if (boosted.isNotEmpty() && Random.nextDouble() < 0.6) boosted else candidates
        } else {
            candidates
        }

        val selected = weightedCandidates.random()

        // 수량 계산: 최대 투자금 범위 내에서 랜덤 비율
        val investmentRatio = Random.nextDouble(0.3, 1.0)
        val investAmount = (maxInvestment * investmentRatio).toLong()
        val quantity = (investAmount / selected.currentPrice).coerceAtLeast(1)

        log.info("매매 결정: BUY - investor={}, stock={}, qty={}", request.investorName, selected.stockId, quantity)

        return TradingDecisionResult(
            action = "BUY",
            stockId = selected.stockId,
            quantity = quantity
        )
    }

    private fun generateSellDecision(request: TradingDecisionRequest): TradingDecisionResult {
        if (request.currentHoldings.isEmpty()) {
            return TradingDecisionResult(action = "HOLD", stockId = null, quantity = 0)
        }

        // 보유 종목 중 랜덤 선택
        val holding = request.currentHoldings.random()

        // 투자 성향별 매도 비율
        val sellRatio = when (request.investmentStyle) {
            "AGGRESSIVE" -> Random.nextDouble(0.3, 1.0)
            "STABLE" -> Random.nextDouble(0.1, 0.5)
            "VALUE" -> Random.nextDouble(0.2, 0.7)
            else -> Random.nextDouble(0.1, 1.0)
        }

        val quantity = (holding.quantity * sellRatio).toLong().coerceAtLeast(1).coerceAtMost(holding.quantity)

        log.info("매매 결정: SELL - investor={}, stock={}, qty={}", request.investorName, holding.stockId, quantity)

        return TradingDecisionResult(
            action = "SELL",
            stockId = holding.stockId,
            quantity = quantity
        )
    }
}
