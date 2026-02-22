package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.eventservice.application.port.out.institution.InstitutionNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InstitutionNameGeneratorWithLLM(
    private val llmExecutor: PromptExecutor
) : InstitutionNameGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun generate(type: InstitutionType): String {
        val typePrompt = when (type) {
            InstitutionType.INSTITUTIONAL_INVESTOR -> "한국 금융시장의 가상 기관투자자(증권사) 이름을 1개만 생성해주세요."
            InstitutionType.FOREIGN_INVESTOR -> "한국 금융시장에 투자하는 가상 외국계 투자기관 이름을 1개만 생성해주세요."
            InstitutionType.PENSION_FUNDS -> "한국의 가상 연기금/공적기금 이름을 1개만 생성해주세요."
            InstitutionType.ASSET_MANAGEMENT -> "한국의 가상 자산운용사 이름을 1개만 생성해주세요."
        }

        val prompt = """
            $typePrompt
            규칙:
            - 실제 한국 금융기관처럼 자연스러운 이름
            - 3~8글자
            - 기관명만 출력 (설명 없이)
        """.trimIndent()

        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            try {
                val agent = AIAgent(
                    promptExecutor = llmExecutor,
                    llmModel = AnthropicModels.Haiku_4_5
                )
                val name = agent.run(prompt).trim()
                log.debug("기관 이름 생성 성공: {} (유형: {}, 시도 {}회)", name, type.displayName, attempt + 1)
                return name
            } catch (e: Exception) {
                log.warn("LLM 기관 이름 생성 실패 (시도 {}회, 유형: {}): {}", attempt + 1, type.displayName, e.message)
            }
        }

        throw RuntimeException("기관 이름 생성 ${maxRetries}회 시도 실패: type=${type.displayName}")
    }
}
