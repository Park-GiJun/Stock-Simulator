package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.eventservice.application.port.out.stock.CompanyNameGeneratePort
import com.stocksimulator.eventservice.application.port.out.stock.StockExistenceCheckPort
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

@Primary
@Component
class CompanyNameGeneratorWithLLM(
    private val llmExecutor: PromptExecutor,
    private val stockExistenceCheckPort: StockExistenceCheckPort
) : CompanyNameGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun generate(sector: Sector): String {
        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            val prompt = """
                한국 주식시장에 신규 상장하는 가상의 ${sector.displayName} 기업 이름을 1개만 생성해주세요.
                규칙:
                - 실제 한국 기업처럼 자연스러운 이름
                - 2~5글자의 한글 또는 한글+영문 조합
                - 회사명만 출력 (설명 없이)
            """.trimIndent()

            val agent = AIAgent(
                promptExecutor = llmExecutor,
                llmModel = AnthropicModels.Haiku_4_5
            )
            val name = agent.run(prompt).trim()

            val exists = try {
                stockExistenceCheckPort.existsByStockName(name)
            } catch (e: Exception) {
                log.warn("stock-service 호출 실패 (stockName={}): {}", name, e.message)
                false
            }

            if (!exists) {
                log.debug("회사명 생성 성공: {} (시도 {}회)", name, attempt + 1)
                return name
            }
            log.info("회사명 중복 발견: {} (시도 {}회), 재생성 시도", name, attempt + 1)
        }

        val fallbackPrompt = """
            한국 주식시장에 신규 상장하는 가상의 ${sector.displayName} 기업 이름을 1개만 생성해주세요.
            규칙:
            - 실제 한국 기업처럼 자연스러운 이름
            - 2~5글자의 한글 또는 한글+영문 조합
            - 회사명만 출력 (설명 없이)
        """.trimIndent()

        val agent = AIAgent(
            promptExecutor = llmExecutor,
            llmModel = AnthropicModels.Haiku_4_5
        )
        val baseName = agent.run(fallbackPrompt).trim()
        val fallbackName = "$baseName${Random.nextInt(10, 100)}"
        log.warn("회사명 재시도 초과, fallback 이름 사용: {}", fallbackName)
        return fallbackName
    }

    override fun generateStockCode(): String {
        val maxRetries = 5

        repeat(maxRetries) {
            val uuid = UUID.randomUUID().toString().replace("-", "").take(20)
            val exists = try {
                stockExistenceCheckPort.existsByStockId(uuid)
            } catch (e: Exception) {
                log.warn("stock-service 호출 실패 (stockId={}): {}", uuid, e.message)
                false
            }

            if (!exists) {
                return uuid
            }
            log.info("주식코드 중복 발견: {}, 재생성 시도", uuid)
        }

        return UUID.randomUUID().toString().replace("-", "").take(20)
    }
}
