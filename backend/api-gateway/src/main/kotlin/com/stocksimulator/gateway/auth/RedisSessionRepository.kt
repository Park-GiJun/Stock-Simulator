package com.stocksimulator.gateway.auth

import com.stocksimulator.gateway.config.AuthProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class RedisSessionRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    private val authProperties: AuthProperties
) {
    private val log = LoggerFactory.getLogger(RedisSessionRepository::class.java)

    fun getSessionData(sessionId: String): Mono<SessionData> {
        val sessionKey = "${authProperties.sessionNamespace}:sessions:$sessionId"
        log.debug("Looking up session: key={}", sessionKey)

        return redisTemplate.opsForHash<String, Any>()
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

    private fun extractSessionData(entries: Map<String, Any>, sessionId: String): Mono<SessionData> {
        val raw = entries["sessionAttr:userId"]
        log.debug("Session userId raw: type={}, value={}", raw?.javaClass?.name, raw)

        val userId = when (raw) {
            is Long -> raw
            is Number -> raw.toLong()
            is String -> raw.toLongOrNull()
            is ByteArray -> tryDeserializeBytes(raw)
            else -> null
        }

        return if (userId != null) {
            val role = entries["sessionAttr:role"]?.toString()
            Mono.just(SessionData(userId = userId, role = role))
        } else {
            log.warn(
                "Session exists but userId is missing: sessionId={}, keys={}, rawType={}",
                sessionId, entries.keys, raw?.javaClass?.name
            )
            Mono.empty()
        }
    }

    private fun tryDeserializeBytes(bytes: ByteArray): Long? {
        return try {
            val ois = java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes))
            when (val obj = ois.readObject()) {
                is Long -> obj
                is Number -> obj.toLong()
                else -> null
            }
        } catch (e: Exception) {
            log.error("Failed to deserialize userId bytes: {}", e.message)
            null
        }
    }

    fun sessionExists(sessionId: String): Mono<Boolean> {
        val sessionKey = "${authProperties.sessionNamespace}:sessions:$sessionId"
        return redisTemplate.hasKey(sessionKey)
    }
}

data class SessionData(
    val userId: Long,
    val role: String? = null
)
