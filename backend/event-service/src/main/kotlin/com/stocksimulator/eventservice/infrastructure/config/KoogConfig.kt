package com.stocksimulator.eventservice.infrastructure.config

import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KoogConfig {
    @Bean
    fun llmExecutor(
        @Value($$"${koog.anthropic.api-key}") apiKey: String
    ): PromptExecutor {
        return simpleAnthropicExecutor(apiKey)
    }
}
