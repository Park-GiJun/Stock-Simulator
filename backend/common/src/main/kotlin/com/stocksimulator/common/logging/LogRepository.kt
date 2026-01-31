package com.stocksimulator.common.logging

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 로그 MongoDB Repository
 */
@Repository
interface LogRepository : MongoRepository<LogDocument, String> {

    /**
     * 특정 서비스의 로그 조회
     */
    fun findByServiceNameOrderByTimestampDesc(serviceName: String): List<LogDocument>

    /**
     * 특정 레벨 이상의 로그 조회
     */
    fun findByLevelInOrderByTimestampDesc(levels: List<String>): List<LogDocument>

    /**
     * 트레이스 ID로 로그 추적
     */
    fun findByTraceIdOrderByTimestamp(traceId: String): List<LogDocument>

    /**
     * 시간 범위로 로그 조회
     */
    fun findByTimestampBetweenOrderByTimestampDesc(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<LogDocument>

    /**
     * 특정 서비스의 에러 로그
     */
    fun findByServiceNameAndLevelOrderByTimestampDesc(
        serviceName: String,
        level: String
    ): List<LogDocument>
}
