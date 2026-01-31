package com.stocksimulator.apigateway.filter

import com.stocksimulator.common.logging.CustomLogger
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * API Gateway 요청/응답 로깅 필터
 *
 * 모든 요청을 가로채서 로그를 MongoDB에 저장
 * - TraceId 생성 및 MDC 설정
 * - 요청 정보 (method, path, clientIp)
 * - 응답 정보 (statusCode, duration)
 */
@Component
class RequestResponseLoggingFilter : GlobalFilter, Ordered {

    private val log = CustomLogger(RequestResponseLoggingFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        val request = exchange.request
        val traceId = UUID.randomUUID().toString().substring(0, 8)

        // TraceId를 헤더에 추가 (다운스트림 서비스로 전파)
        val mutatedRequest = request.mutate()
            .header("X-Trace-Id", traceId)
            .build()

        val mutatedExchange = exchange.mutate().request(mutatedRequest).build()

        // MDC에 TraceId 설정
        CustomLogger.setTraceId(traceId)

        return chain.filter(mutatedExchange)
            .doOnSuccess {
                logRequest(mutatedExchange, startTime, traceId)
            }
            .doOnError { error ->
                logRequest(mutatedExchange, startTime, traceId, error)
            }
            .doFinally {
                CustomLogger.clearTraceId()
            }
    }

    private fun logRequest(
        exchange: ServerWebExchange,
        startTime: Long,
        traceId: String,
        error: Throwable? = null
    ) {
        val request = exchange.request
        val response = exchange.response
        val duration = System.currentTimeMillis() - startTime

        val method = request.method.name()
        val path = request.uri.path
        val statusCode = response.statusCode?.value()
        val clientIp = request.remoteAddress?.address?.hostAddress ?: "unknown"

        val metadata = mutableMapOf<String, Any?>(
            "method" to method,
            "path" to path,
            "statusCode" to statusCode,
            "duration" to duration,
            "clientIp" to clientIp,
            "userAgent" to request.headers.getFirst("User-Agent"),
            "queryParams" to request.queryParams.toString()
        )

        if (error != null) {
            metadata["errorMessage"] = error.message
            metadata["errorType"] = error.javaClass.simpleName
        }

        // 로그 메시지
        val message = buildString {
            append("$method $path")
            if (statusCode != null) append(" -> $statusCode")
            append(" (${duration}ms)")
            if (error != null) append(" [ERROR: ${error.message}]")
        }

        when {
            error != null -> log.error(message, error, metadata)
            statusCode != null && statusCode >= 400 -> log.warn(message, null, metadata)
            else -> log.info(message, metadata)
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
