package com.stocksimulator.common.logging

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * MongoDB에 저장될 로그 문서
 *
 * 모든 마이크로서비스의 로그를 중앙 집중식으로 저장
 *
 * @property id MongoDB 문서 ID
 * @property timestamp 로그 발생 시간
 * @property serviceName 서비스명 (user-service, stock-service 등)
 * @property level 로그 레벨 (DEBUG, INFO, WARN, ERROR)
 * @property traceId 요청 추적 ID (MDC)
 * @property threadName 스레드명
 * @property logger 로거명 (보통 클래스명)
 * @property message 로그 메시지
 * @property stackTrace 예외 스택트레이스 (에러 발생 시)
 * @property metadata 추가 메타데이터 (userId, orderId 등)
 * @property method HTTP 메서드 (GET, POST 등) - API 로그용
 * @property path 요청 경로 - API 로그용
 * @property statusCode HTTP 상태 코드 - API 로그용
 * @property duration 처리 시간 (ms) - API 로그용
 * @property clientIp 클라이언트 IP - API 로그용
 */
@Document(collection = "application_logs")
data class LogDocument(
    @Id
    val id: String? = null,

    @Indexed
    val timestamp: LocalDateTime,

    @Indexed
    val serviceName: String,

    @Indexed
    val level: String,

    @Indexed
    val traceId: String? = null,

    val threadName: String,

    val logger: String,

    val message: String,

    val stackTrace: String? = null,

    val metadata: Map<String, Any?> = emptyMap(),

    // API 로깅 전용 필드
    val method: String? = null,
    val path: String? = null,
    val statusCode: Int? = null,
    val duration: Long? = null,
    val clientIp: String? = null
) {
    companion object {
        /**
         * Logback 이벤트로부터 LogDocument 생성
         */
        fun from(
            event: ch.qos.logback.classic.spi.ILoggingEvent,
            serviceName: String,
            metadata: Map<String, Any?> = emptyMap()
        ): LogDocument {
            return LogDocument(
                timestamp = LocalDateTime.now(),
                serviceName = serviceName,
                level = event.level.toString(),
                traceId = event.mdcPropertyMap["traceId"],
                threadName = event.threadName,
                logger = event.loggerName,
                message = event.formattedMessage,
                stackTrace = if (event.throwableProxy != null) {
                    ch.qos.logback.core.CoreConstants.EMPTY_STRING +
                        ch.qos.logback.classic.spi.ThrowableProxyUtil.asString(event.throwableProxy)
                } else null,
                metadata = metadata
            )
        }

        /**
         * API 로그용 LogDocument 생성
         */
        fun forApi(
            serviceName: String,
            traceId: String?,
            method: String,
            path: String,
            statusCode: Int?,
            duration: Long,
            clientIp: String?,
            metadata: Map<String, Any?> = emptyMap()
        ): LogDocument {
            return LogDocument(
                timestamp = LocalDateTime.now(),
                serviceName = serviceName,
                level = if (statusCode != null && statusCode >= 400) "WARN" else "INFO",
                traceId = traceId,
                threadName = Thread.currentThread().name,
                logger = "API_LOG",
                message = "$method $path - $statusCode (${duration}ms)",
                method = method,
                path = path,
                statusCode = statusCode,
                duration = duration,
                clientIp = clientIp,
                metadata = metadata
            )
        }
    }
}
