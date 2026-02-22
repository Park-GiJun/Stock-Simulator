package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.Sentiment
import com.stocksimulator.eventservice.application.port.out.news.GeneratedNewsContent
import com.stocksimulator.eventservice.application.port.out.news.NewsContentGeneratePort
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NewsContentGeneratorWithLLM(
    private val llmExecutor: PromptExecutor
) : NewsContentGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class LlmNewsResponse(
        val headline: String,
        val sentiment: String,
        val intensity: Double,
        val duration: Long
    )

    override suspend fun generateNews(level: EventLevel, sector: Sector?, stockName: String?): GeneratedNewsContent {
        val prompt = buildPrompt(level, sector, stockName)
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
                log.warn("LLM 뉴스 생성 실패 (시도 {}회): level={}, sector={}, error={}", attempt + 1, level, sector, e.message)
            }
        }

        throw RuntimeException("LLM 뉴스 생성 ${maxRetries}회 시도 실패: level=$level, sector=$sector")
    }

    private fun buildPrompt(level: EventLevel, sector: Sector?, stockName: String?): String {
        val levelDesc = when (level) {
            EventLevel.COMPANY -> "특정 기업에 영향을 미치는 기업 뉴스"
            EventLevel.INDUSTRY -> "특정 산업 전체에 영향을 미치는 산업 뉴스"
            EventLevel.SOCIETY -> "사회 전반에 영향을 미치는 사회/경제 뉴스"
        }

        val sectorDesc = if (sector != null) {
            "관련 산업: ${sector.displayName} (${sector.name})"
        } else {
            "특정 산업에 국한되지 않는 범사회적 뉴스"
        }

        val stockDesc = if (stockName != null) {
            "\n대상 기업: $stockName (이 종목명을 반드시 headline에 포함)"
        } else {
            ""
        }

        return """
            한국 가상 주식시장 시뮬레이터의 ${levelDesc}를 1건 생성해주세요.

            $sectorDesc$stockDesc

            규칙:
            - headline: 한국어 뉴스 헤드라인 (20~40자)
            - sentiment: POSITIVE 또는 NEGATIVE 또는 NEUTRAL 중 하나
            - intensity: 영향 강도 (0.1 ~ 1.0 사이 소수)
            - duration: 영향 지속시간 (게임 시간 기준 분, 30 ~ 480)

            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:
            {"headline":"...","sentiment":"...","intensity":0.5,"duration":120}
        """.trimIndent()
    }

    private fun parseLlmResponse(raw: String): GeneratedNewsContent {
        val jsonStr = extractJson(raw)
        val parsed = json.decodeFromString<LlmNewsResponse>(jsonStr)

        val sentiment = try {
            Sentiment.valueOf(parsed.sentiment.uppercase())
        } catch (e: IllegalArgumentException) {
            Sentiment.NEUTRAL
        }

        return GeneratedNewsContent(
            headline = parsed.headline,
            content = "",
            sentiment = sentiment,
            intensity = parsed.intensity.coerceIn(0.1, 1.0),
            duration = parsed.duration.coerceIn(30, 480)
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
