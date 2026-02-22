package com.stocksimulator.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Authentication configuration properties for API Gateway
 *
 * Configures session validation and public path patterns.
 *
 * Properties:
 * - sessionNamespace: Redis session key prefix (default: "stock-simulator:session")
 * - sessionCookieName: Cookie name containing session ID (default: "SESSION")
 * - publicPaths: List of Ant-style path patterns that don't require authentication
 */
@ConfigurationProperties(prefix = "gateway.auth")
data class AuthProperties(
    val sessionNamespace: String = "stock-simulator:session",
    val sessionCookieName: String = "SESSION",
    val publicPaths: List<String> = listOf(
        "/user-service/api/v1/users/signup",
        "/user-service/api/v1/users/login",
        "/user-service/api/v1/health/**",
        "/**/actuator/**",
        "/**/swagger-ui/**",
        "/**/api-docs/**",
        "/**/v3/api-docs/**",
        "/event-service/api/news/**"
    )
)
