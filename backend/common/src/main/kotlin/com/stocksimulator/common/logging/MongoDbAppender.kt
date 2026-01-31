package com.stocksimulator.common.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * MongoDB에 로그를 저장하는 커스텀 Logback Appender
 *
 * 비동기 처리를 통해 로깅이 애플리케이션 성능에 영향을 주지 않도록 함
 */
class MongoDbAppender : AppenderBase<ILoggingEvent>() {

    private var serviceName: String = "unknown-service"
    private var mongoUri: String = ""
    private var databaseName: String = "stocksimulator"
    private var enabled: Boolean = true

    private lateinit var mongoTemplate: MongoTemplate
    private val logQueue = ConcurrentLinkedQueue<LogDocument>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        @Volatile
        private var instance: MongoDbAppender? = null

        /**
         * 싱글톤 인스턴스 반환
         * Spring Bean이 아닌 Logback 설정에서 사용되므로 수동으로 관리
         */
        fun getInstance(): MongoDbAppender? = instance

        /**
         * MongoTemplate을 외부에서 주입
         * LoggingConfig에서 호출됨
         */
        fun setMongoTemplate(template: MongoTemplate) {
            instance?.mongoTemplate = template
        }
    }

    override fun start() {
        if (!enabled) {
            addWarn("MongoDbAppender is disabled")
            return
        }

        instance = this

        // MongoTemplate은 LoggingConfig에서 주입됨
        // 여기서는 초기화만 수행
        super.start()

        // 배치 처리 코루틴 시작
        startBatchProcessor()
    }

    override fun stop() {
        // 남은 로그 모두 처리
        flushLogs()
        super.stop()
    }

    override fun append(event: ILoggingEvent) {
        if (!enabled || !isStarted) {
            return
        }

        try {
            // MDC에서 추가 메타데이터 추출
            val metadata = mutableMapOf<String, Any?>()
            event.mdcPropertyMap?.forEach { (k, v) ->
                if (k != "traceId") { // traceId는 이미 별도 필드로 저장
                    metadata[k] = v
                }
            }

            val logDoc = LogDocument.from(event, serviceName, metadata)
            logQueue.offer(logDoc)

        } catch (e: Exception) {
            addError("Failed to append log to MongoDB", e)
        }
    }

    /**
     * 배치 처리로 MongoDB에 저장
     * 큐에 쌓인 로그를 주기적으로 일괄 저장
     */
    private fun startBatchProcessor() {
        coroutineScope.launch {
            while (isStarted) {
                try {
                    kotlinx.coroutines.delay(1000) // 1초마다 처리
                    flushLogs()
                } catch (e: Exception) {
                    addError("Error in batch processor", e)
                }
            }
        }
    }

    /**
     * 큐에 있는 로그를 MongoDB에 일괄 저장
     */
    private fun flushLogs() {
        if (!::mongoTemplate.isInitialized || logQueue.isEmpty()) {
            return
        }

        try {
            val logs = mutableListOf<LogDocument>()
            while (logs.size < 100) { // 최대 100개씩 배치 처리
                val log = logQueue.poll() ?: break
                logs.add(log)
            }

            if (logs.isNotEmpty()) {
                mongoTemplate.insertAll(logs)
            }
        } catch (e: Exception) {
            addError("Failed to flush logs to MongoDB", e)
        }
    }

    // Logback XML에서 설정할 수 있는 setter 메서드들
    fun setServiceName(name: String) {
        this.serviceName = name
    }

    fun setMongoUri(uri: String) {
        this.mongoUri = uri
    }

    fun setDatabaseName(name: String) {
        this.databaseName = name
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
