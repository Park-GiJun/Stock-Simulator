package com.stocksimulator.gateway.config

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties
import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springframework.cloud.gateway.route.RouteDefinition
import org.springframework.cloud.gateway.route.RouteDefinitionLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

/**
 * API Gateway의 Swagger UI 설정
 * 
 * 각 마이크로서비스의 OpenAPI 문서를 통합하여 표시
 */
@Configuration
class SwaggerConfig {

    /**
     * 각 서비스의 OpenAPI 문서 URL을 동적으로 설정
     * 
     * Swagger UI에서 드롭다운으로 각 서비스별 API 문서를 선택할 수 있도록 함
     */
    @Bean
    @Lazy(false)
    fun swaggerUiConfigProperties(
        routeDefinitionLocator: RouteDefinitionLocator
    ): SwaggerUiConfigProperties {
        val config = SwaggerUiConfigProperties()
        
        // 각 서비스의 API 문서 URL을 추가
        val urls = mutableSetOf<AbstractSwaggerUiConfigProperties.SwaggerUrl>()
        
        // 수동으로 정의된 서비스 목록
        val services = listOf(
            "user-service" to "User Service",
            "stock-service" to "Stock Service", 
            "trading-service" to "Trading Service",
            "event-service" to "Event Service",
            "news-service" to "News Service",
            "scheduler-service" to "Scheduler Service"
        )
        
        services.forEach { (serviceName, displayName) ->
            val swaggerUrl = AbstractSwaggerUiConfigProperties.SwaggerUrl(
                displayName,
                "/$serviceName/v3/api-docs",
                displayName
            )
            urls.add(swaggerUrl)
        }
        
        config.urls = urls
        
        return config
    }
}
