package com.stocksimulator.schedulerservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.schedulerservice.application.port.out.NpcNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class NpcNameGeneratorWithLLM(
    private val llmExecutor: PromptExecutor
) : NpcNameGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun generate(): String {
        val prompt = """
            한국인 가상 개인 투자자 이름을 1개만 생성해주세요.
            규칙:
            - 실제 한국인 이름처럼 자연스러운 이름
            - 2~4글자
            - 이름만 출력 (설명 없이)
        """.trimIndent()

        return try {
            val agent = AIAgent(
                promptExecutor = llmExecutor,
                llmModel = AnthropicModels.Haiku_4_5
            )
            val name = agent.run(prompt).trim()
            log.debug("NPC 이름 생성 성공: {}", name)
            name
        } catch (e: Exception) {
            log.warn("LLM NPC 이름 생성 실패: {}, fallback 사용", e.message)
            generateFallbackName()
        }
    }

    private fun generateFallbackName(): String {
        val fallbackName = "투자자${Random.nextInt(100, 999)}"
        log.warn("fallback NPC 이름 사용: {}", fallbackName)
        return fallbackName
    }
}
