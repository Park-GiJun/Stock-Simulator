package com.stocksimulator.gateway.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteDefinition
import org.springframework.cloud.gateway.route.RouteDefinitionLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class SwaggerConfig(
    private val routeDefinitionLocator: RouteDefinitionLocator
) {

    /**
     * User Service API Documentation
     */
    @Bean
    fun userServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("user-service")
            .pathsToMatch("/user-service/**")
            .build()
    }

    /**
     * Stock Service API Documentation
     */
    @Bean
    fun stockServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("stock-service")
            .pathsToMatch("/stock-service/**")
            .build()
    }

    /**
     * Trading Service API Documentation
     */
    @Bean
    fun tradingServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("trading-service")
            .pathsToMatch("/trading-service/**")
            .build()
    }

    /**
     * Event Service API Documentation
     */
    @Bean
    fun eventServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("event-service")
            .pathsToMatch("/event-service/**")
            .build()
    }

    /**
     * News Service API Documentation
     */
    @Bean
    fun newsServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("news-service")
            .pathsToMatch("/news-service/**")
            .build()
    }

    /**
     * Scheduler Service API Documentation
     */
    @Bean
    fun schedulerServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("scheduler-service")
            .pathsToMatch("/scheduler-service/**")
            .build()
    }

    /**
     * All Services API Documentation
     */
    @Bean
    @Primary
    fun allServicesApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("all-services")
            .pathsToMatch("/**")
            .build()
    }
}
