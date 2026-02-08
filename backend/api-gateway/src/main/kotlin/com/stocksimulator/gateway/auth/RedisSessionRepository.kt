package com.stocksimulator.gateway.auth

import com.stocksimulator.gateway.config.AuthProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

/**
 * Redis Session Repository for API Gateway
 *
 * Queries Redis to validate sessions and extract user information.
 * Session data is stored by user-service using Spring Session.
 *
 * Key pattern: {namespace}:sessions:{sessionId}
 * Hash fields:
 *   - sessionAttr:userId: User ID (Long)
 *   - sessionAttr:role: User role (optional)
 */
@Repository
class RedisSessionRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val authProperties: AuthProperties
) {
    private val log = LoggerFactory.getLogger(RedisSessionRepository::class.java)

    /**
     * Retrieves session data from Redis
     *
     * @param sessionId The session ID from cookie
     * @return SessionData if valid session exists, empty Mono otherwise
     */
    fun getSessionData(sessionId: String): Mono<SessionData> {
        val sessionKey = "${authProperties.sessionNamespace}:sessions:$sessionId"
        log.debug("Looking up session: key={}", sessionKey)

        return redisTemplate.opsForHash<String, String>()
            .entries(sessionKey)
            .collectMap({ it.key }, { it.value })
            .flatMap { entries ->
                if (entries.isEmpty()) {
                    log.debug("Session not found: sessionId={}", sessionId)
                    Mono.empty()
                } else {
                    extractSessionData(entries, sessionId)
                }
            }
            .doOnNext { data ->
                log.debug("Session found: sessionId={}, userId={}", sessionId, data.userId)
            }
            .doOnError { error ->
                log.error("Error retrieving session: sessionId={}, error={}", sessionId, error.message)
            }
    }

    private fun extractSessionData(entries: Map<String, String>, sessionId: String): Mono<SessionData> {
        // Spring Session stores attributes with "sessionAttr:" prefix
        val userId = entries["sessionAttr:userId"]?.toLongOrNull()

        return if (userId != null) {
            // Role may or may not be stored in session
            val role = entries["sessionAttr:role"]
            Mono.just(SessionData(userId = userId, role = role))
        } else {
            log.warn("Session exists but userId is missing: sessionId={}", sessionId)
            Mono.empty()
        }
    }

    /**
     * Checks if a session exists in Redis
     *
     * @param sessionId The session ID to check
     * @return true if session exists
     */
    fun sessionExists(sessionId: String): Mono<Boolean> {
        val sessionKey = "${authProperties.sessionNamespace}:sessions:$sessionId"
        return redisTemplate.hasKey(sessionKey)
    }
}

/**
 * Session data extracted from Redis
 */
data class SessionData(
    val userId: Long,
    val role: String? = null
)
