package com.stocksimulator.common.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            // Java 8 Date/Time 지원
            registerModule(JavaTimeModule())

            // 날짜를 timestamp가 아닌 ISO-8601 형식으로
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            // 알 수 없는 속성 무시
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            // null 값 직렬화
            // enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
        }
    }
}
