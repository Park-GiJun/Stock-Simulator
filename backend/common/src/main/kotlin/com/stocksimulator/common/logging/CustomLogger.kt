package com.stocksimulator.common.logging

import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * MongoDB 로깅을 지원하는 커스텀 Logger 래퍼
 *
 * 기존 SLF4J Logger를 래핑하여 구조화된 로깅을 제공
 * 메타데이터를 MDC에 저장하여 MongoDbAppender가 수집할 수 있도록 함
 *
 * 사용 예시:
 * ```kotlin
 * class UserController {
 *     private val log = CustomLogger(UserController::class.java)
 *
 *     fun register(user: User) {
 *         log.info("User registered", mapOf(
 *             "userId" to user.id,
 *             "email" to user.email
 *         ))
 *     }
 * }
 * ```
 */
class CustomLogger(clazz: Class<*>) {

    private val logger = LoggerFactory.getLogger(clazz)
    private val loggerName = clazz.simpleName

    /**
     * TRACE 레벨 로그
     */
    fun trace(message: String, metadata: Map<String, Any?> = emptyMap()) {
        if (logger.isTraceEnabled) {
            withMetadata(metadata) {
                logger.trace(message)
            }
        }
    }

    /**
     * DEBUG 레벨 로그
     */
    fun debug(message: String, metadata: Map<String, Any?> = emptyMap()) {
        if (logger.isDebugEnabled) {
            withMetadata(metadata) {
                logger.debug(message)
            }
        }
    }

    /**
     * INFO 레벨 로그
     */
    fun info(message: String, metadata: Map<String, Any?> = emptyMap()) {
        if (logger.isInfoEnabled) {
            withMetadata(metadata) {
                logger.info(message)
            }
        }
    }

    /**
     * WARN 레벨 로그
     */
    fun warn(message: String, throwable: Throwable? = null, metadata: Map<String, Any?> = emptyMap()) {
        if (logger.isWarnEnabled) {
            withMetadata(metadata) {
                if (throwable != null) {
                    logger.warn(message, throwable)
                } else {
                    logger.warn(message)
                }
            }
        }
    }

    /**
     * ERROR 레벨 로그
     */
    fun error(message: String, throwable: Throwable? = null, metadata: Map<String, Any?> = emptyMap()) {
        if (logger.isErrorEnabled) {
            withMetadata(metadata) {
                if (throwable != null) {
                    logger.error(message, throwable)
                } else {
                    logger.error(message)
                }
            }
        }
    }

    /**
     * API 요청/응답 전용 로그
     */
    fun api(
        method: String,
        path: String,
        statusCode: Int?,
        duration: Long,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        val apiMetadata = mutableMapOf<String, Any?>()
        apiMetadata["method"] = method
        apiMetadata["path"] = path
        apiMetadata["statusCode"] = statusCode
        apiMetadata["duration"] = duration
        apiMetadata.putAll(metadata)

        val message = "$method $path - ${statusCode ?: "N/A"} (${duration}ms)"
        if (statusCode != null && statusCode >= 400) {
            warn(message, null, apiMetadata)
        } else {
            info(message, apiMetadata)
        }
    }

    /**
     * 메타데이터를 MDC에 추가한 후 로깅 실행, 완료 후 MDC 정리
     */
    private inline fun withMetadata(metadata: Map<String, Any?>, block: () -> Unit) {
        try {
            // 메타데이터를 MDC에 추가
            metadata.forEach { (key, value) ->
                MDC.put(key, value?.toString() ?: "null")
            }

            // 로깅 실행
            block()
        } finally {
            // MDC 정리
            metadata.keys.forEach { key ->
                MDC.remove(key)
            }
        }
    }

    companion object {
        /**
         * 클래스로부터 CustomLogger 인스턴스 생성
         */
        inline fun <reified T> logger(): CustomLogger {
            return CustomLogger(T::class.java)
        }

        /**
         * TraceId 설정 (필터나 인터셉터에서 사용)
         */
        fun setTraceId(traceId: String) {
            MDC.put("traceId", traceId)
        }

        /**
         * TraceId 조회
         */
        fun getTraceId(): String? {
            return MDC.get("traceId")
        }

        /**
         * TraceId 제거
         */
        fun clearTraceId() {
            MDC.remove("traceId")
        }

        /**
         * 모든 MDC 정리
         */
        fun clearAll() {
            MDC.clear()
        }
    }
}
