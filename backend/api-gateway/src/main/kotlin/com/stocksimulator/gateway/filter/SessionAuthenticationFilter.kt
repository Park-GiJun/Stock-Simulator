package com.stocksimulator.gateway.filter

import com.stocksimulator.gateway.auth.RedisSessionRepository
import com.stocksimulator.gateway.config.AuthProperties
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpCookie
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Session Authentication Filter for API Gateway
 *
 * Validates Redis session for all non-public requests and propagates
 * user information to downstream services via headers.
 *
 * Flow:
 * 1. Check if path is public → skip authentication
 * 2. Extract SESSION cookie
 * 3. Validate session in Redis
 * 4. Add X-User-Id and X-User-Role headers
 * 5. Return 401 Unauthorized for invalid/missing sessions
 */
@Component
class SessionAuthenticationFilter(
    private val sessionRepository: RedisSessionRepository,
    private val authProperties: AuthProperties
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(SessionAuthenticationFilter::class.java)
    private val pathMatcher = AntPathMatcher()

    companion object {
        const val X_USER_ID_HEADER = "X-User-Id"
        const val X_USER_ROLE_HEADER = "X-User-Role"
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.uri.path

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            log.debug("Public path, skipping authentication: {}", path)
            return chain.filter(exchange)
        }

        // Extract session cookie
        val sessionCookie = extractSessionCookie(exchange)
        if (sessionCookie == null) {
            log.debug("No session cookie found for path: {}", path)
            return unauthorized(exchange, "Authentication required")
        }

        val sessionId = sessionCookie.value
        log.debug("Validating session: sessionId={}, path={}", sessionId, path)

        // Validate session in Redis
        return sessionRepository.getSessionData(sessionId)
            .flatMap { sessionData ->
                // Set MDC tags for logging context
                MDC.put("userId", sessionData.userId.toString())
                MDC.put("sessionId", sessionId)
                sessionData.role?.let { MDC.put("userRole", it) }

                log.info(
                    "Session validated: userId={}, role={}, path={}",
                    sessionData.userId,
                    sessionData.role ?: "N/A",
                    path
                )

                // Add user info headers to the request
                val mutatedRequest = request.mutate()
                    .header(X_USER_ID_HEADER, sessionData.userId.toString())
                    .apply {
                        sessionData.role?.let { role ->
                            header(X_USER_ROLE_HEADER, role)
                        }
                    }
                    .build()

                val mutatedExchange = exchange.mutate().request(mutatedRequest).build()
                chain.filter(mutatedExchange)
            }
            .switchIfEmpty(
                Mono.defer {
                    log.warn("Invalid or expired session: sessionId={}, path={}", sessionId, path)
                    unauthorized(exchange, "Session expired or invalid")
                }
            )
            .doFinally {
                // Clear MDC tags after request completes
                MDC.remove("userId")
                MDC.remove("sessionId")
                MDC.remove("userRole")
            }
    }

    /**
     * Check if the path matches any public path pattern
     */
    private fun isPublicPath(path: String): Boolean {
        return authProperties.publicPaths.any { pattern ->
            pathMatcher.match(pattern, path)
        }
    }

    /**
     * Extract session cookie from request
     */
    private fun extractSessionCookie(exchange: ServerWebExchange): HttpCookie? {
        val cookieName = authProperties.sessionCookieName
        return exchange.request.cookies[cookieName]?.firstOrNull()
    }

    /**
     * Return 401 Unauthorized response
     */
    private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON

        val body = """{"success":false,"error":"$message","code":"UNAUTHORIZED"}"""
        val buffer = response.bufferFactory().wrap(body.toByteArray())

        return response.writeWith(Mono.just(buffer))
    }

    /**
     * Filter order: runs after logging filter (HIGHEST_PRECEDENCE)
     * but before routing filters
     */
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 10
}
