package com.stocksimulator.userservice.adapter.`in`.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    // Health check endpoints
                    .pathMatchers("/api/v1/health/**").permitAll()
                    // Actuator endpoints
                    .pathMatchers("/actuator/**").permitAll()
                    // Swagger/OpenAPI endpoints
                    .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/webjars/**").permitAll()
                    // All other endpoints require authentication (for now, permit all for development)
                    .anyExchange().permitAll()
            }
            .build()
    }
}
