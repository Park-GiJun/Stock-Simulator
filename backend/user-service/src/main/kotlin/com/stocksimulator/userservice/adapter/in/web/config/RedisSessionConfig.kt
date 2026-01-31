package com.stocksimulator.userservice.adapter.`in`.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import java.time.Duration

/**
 * Redis Session 설정 (WebFlux 환경)
 *
 * - Session Store: Redis
 * - Session Timeout: 30분
 * - Cookie Name: SESSION
 * - HttpOnly: true
 * - SameSite: Lax
 */
@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 1800) // 30분
class RedisSessionConfig {

    /**
     * Session ID를 Cookie로 전달하는 Resolver 설정
     */
    @Bean
    fun webSessionIdResolver(): WebSessionIdResolver {
        val resolver = CookieWebSessionIdResolver()
        resolver.setCookieName("SESSION")
        resolver.setCookieMaxAge(Duration.ofMinutes(30))
        // HttpOnly, Secure, SameSite는 application.yml에서 설정 가능
        return resolver
    }
}
