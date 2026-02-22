package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.eventservice.application.port.out.npc.NpcNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NpcNameGeneratorWithLLM(
    private val llmExecutor: PromptExecutor
) : NpcNameGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun generate(existingNames: List<String>): String {
        val avoidClause = if (existingNames.isNotEmpty()) {
            val namesToAvoid = existingNames.takeLast(50).joinToString(", ")
            "\n- 다음 이름은 이미 사용중이니 피해주세요: $namesToAvoid"
        } else {
            ""
        }

        val prompt = """
            한국인 가상 개인 투자자 이름을 1개만 생성해주세요.
            규칙:
            - 실제 한국인 이름처럼 자연스러운 이름
            - 2~4글자
            - 이름만 출력 (설명 없이)$avoidClause
        """.trimIndent()

        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            try {
                val agent = AIAgent(
                    promptExecutor = llmExecutor,
                    llmModel = AnthropicModels.Haiku_4_5
                )
                val name = agent.run(prompt).trim()

                if (name !in existingNames) {
                    log.debug("NPC 이름 생성 성공: {} (시도 {}회)", name, attempt + 1)
                    return name
                }
                log.info("NPC 이름 중복 발견: {} (시도 {}회), 재생성 시도", name, attempt + 1)
            } catch (e: Exception) {
                log.warn("LLM NPC 이름 생성 실패 (시도 {}회): {}", attempt + 1, e.message)
            }
        }

        throw RuntimeException("NPC 이름 생성 ${maxRetries}회 시도 실패")
    }
}
