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
import kotlin.random.Random

@Component
class NewsContentGeneratorWithLLM(
    private val llmExecutor: PromptExecutor
) : NewsContentGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class LlmNewsResponse(
        val headline: String,
        val content: String,
        val sentiment: String,
        val intensity: Double,
        val duration: Long
    )

    override suspend fun generateNews(level: EventLevel, sector: Sector?): GeneratedNewsContent {
        val prompt = buildPrompt(level, sector)

        return try {
            val agent = AIAgent(
                promptExecutor = llmExecutor,
                llmModel = AnthropicModels.Haiku_4_5
            )
            val rawResponse = agent.run(prompt).trim()
            parseLlmResponse(rawResponse)
        } catch (e: Exception) {
            log.warn("LLM 뉴스 생성 실패, fallback 사용: level={}, sector={}, error={}", level, sector, e.message)
            generateFallback(level, sector)
        }
    }

    private fun buildPrompt(level: EventLevel, sector: Sector?): String {
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

        return """
            한국 가상 주식시장 시뮬레이터의 ${levelDesc}를 1건 생성해주세요.

            $sectorDesc

            규칙:
            - headline: 한국어 뉴스 제목 (20~40자)
            - content: 한국어 뉴스 본문 (100~200자)
            - sentiment: POSITIVE 또는 NEGATIVE 또는 NEUTRAL 중 하나
            - intensity: 영향 강도 (0.1 ~ 1.0 사이 소수)
            - duration: 영향 지속시간 (게임 시간 기준 분, 30 ~ 480)

            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:
            {"headline":"...","content":"...","sentiment":"...","intensity":0.5,"duration":120}
        """.trimIndent()
    }

    private fun parseLlmResponse(raw: String): GeneratedNewsContent {
        // JSON 블록 추출 (마크다운 코드블록 또는 직접 JSON)
        val jsonStr = extractJson(raw)
        val parsed = json.decodeFromString<LlmNewsResponse>(jsonStr)

        val sentiment = try {
            Sentiment.valueOf(parsed.sentiment.uppercase())
        } catch (e: IllegalArgumentException) {
            Sentiment.NEUTRAL
        }

        return GeneratedNewsContent(
            headline = parsed.headline,
            content = parsed.content,
            sentiment = sentiment,
            intensity = parsed.intensity.coerceIn(0.1, 1.0),
            duration = parsed.duration.coerceIn(30, 480)
        )
    }

    private fun extractJson(raw: String): String {
        // ```json ... ``` 블록 추출
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{.*?})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        val codeBlockMatch = codeBlockRegex.find(raw)
        if (codeBlockMatch != null) {
            return codeBlockMatch.groupValues[1].trim()
        }

        // 직접 JSON 객체 추출
        val jsonRegex = Regex("\\{.*}", RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonRegex.find(raw)
        if (jsonMatch != null) {
            return jsonMatch.value.trim()
        }

        return raw
    }

    private fun generateFallback(level: EventLevel, sector: Sector?): GeneratedNewsContent {
        val sentiment = listOf(Sentiment.POSITIVE, Sentiment.NEGATIVE, Sentiment.NEUTRAL).random()
        val intensity = Random.nextDouble(0.2, 0.8)
        val duration = listOf(60L, 120L, 180L, 240L).random()

        return when (level) {
            EventLevel.COMPANY -> GeneratedNewsContent(
                headline = "${sector?.displayName ?: "기업"} 관련 주요 뉴스 발생",
                content = "${sector?.displayName ?: "해당 기업"} 분야에서 주목할 만한 변화가 감지되었습니다. 시장 전문가들은 향후 추이를 주시하고 있습니다.",
                sentiment = sentiment,
                intensity = intensity,
                duration = duration
            )
            EventLevel.INDUSTRY -> GeneratedNewsContent(
                headline = "${sector?.displayName ?: "산업"} 산업 동향 변화 감지",
                content = "${sector?.displayName ?: "해당"} 산업 전반에 걸쳐 새로운 동향이 포착되었습니다. 관련 종목들의 변동성 확대가 예상됩니다.",
                sentiment = sentiment,
                intensity = intensity,
                duration = duration
            )
            EventLevel.SOCIETY -> GeneratedNewsContent(
                headline = "경제 전반에 영향 미칠 새로운 정책 발표",
                content = "정부의 새로운 경제 정책 발표로 시장 전반에 영향이 예상됩니다. 투자자들의 신중한 대응이 필요합니다.",
                sentiment = sentiment,
                intensity = intensity,
                duration = duration
            )
        }
    }
}
