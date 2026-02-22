package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.eventservice.application.port.out.trading.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TradingDecisionGeneratorWithLLM(
    private val llmExecutor: PromptExecutor
) : TradingDecisionGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class LlmTradingResponse(
        val action: String,
        val stockId: String? = null,
        val quantity: Long = 0
    )

    override suspend fun generateDecision(request: TradingDecisionRequest): TradingDecisionResult {
        val prompt = buildPrompt(request)
        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            try {
                val agent = AIAgent(
                    promptExecutor = llmExecutor,
                    llmModel = AnthropicModels.Haiku_4_5
                )
                val rawResponse = agent.run(prompt).trim()
                return parseLlmResponse(rawResponse)
            } catch (e: Exception) {
                log.warn("LLM 매매 결정 생성 실패 (시도 {}회): investor={}, error={}",
                    attempt + 1, request.investorName, e.message)
            }
        }

        log.warn("LLM 매매 결정 {}회 시도 실패, HOLD 반환: investor={}", maxRetries, request.investorName)
        return TradingDecisionResult(action = "HOLD", stockId = null, quantity = 0)
    }

    private fun buildPrompt(request: TradingDecisionRequest): String {
        val maxInvestmentRate = when (request.investmentStyle) {
            "AGGRESSIVE" -> 30
            "STABLE" -> 10
            "VALUE" -> 20
            else -> 15
        }
        val maxInvestment = request.availableCash * maxInvestmentRate / 100

        val holdingsDesc = if (request.currentHoldings.isEmpty()) {
            "보유 종목 없음"
        } else {
            request.currentHoldings.joinToString("\n") { h ->
                "  - 종목ID: ${h.stockId}, 보유수량: ${h.quantity}주, 평균매입가: ${h.averagePrice}원"
            }
        }

        val candidatesDesc = if (request.stockCandidates.isEmpty()) {
            "매매 후보 종목 없음"
        } else {
            request.stockCandidates.joinToString("\n") { s ->
                "  - 종목ID: ${s.stockId}, 종목명: ${s.stockName}, 섹터: ${s.sector}, 현재가: ${s.currentPrice}원, 변동률: ${s.changePercent}%"
            }
        }

        val newsDesc = if (request.recentNews.isEmpty()) {
            "최근 뉴스 없음"
        } else {
            request.recentNews.joinToString("\n") { n ->
                "  - ${n.headline} (감정: ${n.sentiment}, 섹터: ${n.sector ?: "전체"})"
            }
        }

        val actionsDesc = request.allowedActions.joinToString("|")
        val rulesDesc = buildList {
            if ("BUY" in request.allowedActions) add("BUY(후보중 선택, qty*현재가<=${maxInvestment})")
            if ("SELL" in request.allowedActions) add("SELL(보유중, qty<=보유수량)")
            add("HOLD(매매안함)")
        }.joinToString(", ")

        return """
            투자 결정 시스템. JSON만 응답.

            투자자: ${request.investorName}, 성향: ${request.investmentStyle}, 위험허용도: ${request.riskTolerance}
            현금: ${request.availableCash}원, 최대투자: ${maxInvestment}원, 선호섹터: ${request.preferredSectors.joinToString(",")}

            보유: $holdingsDesc
            후보: $candidatesDesc
            뉴스: $newsDesc

            규칙: $rulesDesc
            JSON만: {"action":"$actionsDesc","stockId":"ID|null","quantity":수량}
        """.trimIndent()
    }

    private fun parseLlmResponse(raw: String): TradingDecisionResult {
        val jsonStr = extractJson(raw)
        val parsed = json.decodeFromString<LlmTradingResponse>(jsonStr)

        val action = parsed.action.uppercase()
        if (action !in listOf("BUY", "SELL", "HOLD")) {
            return TradingDecisionResult(action = "HOLD", stockId = null, quantity = 0)
        }

        return TradingDecisionResult(
            action = action,
            stockId = if (action == "HOLD") null else parsed.stockId,
            quantity = if (action == "HOLD") 0 else parsed.quantity.coerceAtLeast(1)
        )
    }

    private fun extractJson(raw: String): String {
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{.*?})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        val codeBlockMatch = codeBlockRegex.find(raw)
        if (codeBlockMatch != null) {
            return codeBlockMatch.groupValues[1].trim()
        }

        val jsonRegex = Regex("\\{.*}", RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonRegex.find(raw)
        if (jsonMatch != null) {
            return jsonMatch.value.trim()
        }

        return raw
    }
}
