package com.stocksimulator.schedulerservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.schedulerservice.application.port.out.institution.InstitutionNameGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

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

        return try {
            val agent = AIAgent(
                promptExecutor = llmExecutor,
                llmModel = AnthropicModels.Haiku_4_5
            )
            val name = agent.run(prompt).trim()
            log.debug("기관 이름 생성 성공: {} (유형: {})", name, type.displayName)
            name
        } catch (e: Exception) {
            log.warn("LLM 기관 이름 생성 실패 (유형: {}): {}, fallback 사용", type.displayName, e.message)
            generateFallbackName(type)
        }
    }

    private fun generateFallbackName(type: InstitutionType): String {
        val suffix = Random.nextInt(10, 100)
        val baseName = when (type) {
            InstitutionType.INSTITUTIONAL_INVESTOR -> "가상증권"
            InstitutionType.FOREIGN_INVESTOR -> "글로벌투자"
            InstitutionType.PENSION_FUNDS -> "국민연금"
            InstitutionType.ASSET_MANAGEMENT -> "미래자산"
        }
        val fallbackName = "$baseName$suffix"
        log.warn("fallback 기관 이름 사용: {}", fallbackName)
        return fallbackName
    }
}
