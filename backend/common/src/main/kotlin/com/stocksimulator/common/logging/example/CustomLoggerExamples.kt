package com.stocksimulator.common.logging.example

import com.stocksimulator.common.logging.CustomLogger
import org.springframework.stereotype.Service

/**
 * CustomLogger 사용 예시
 *
 * 이 파일은 교육 목적의 예시입니다.
 * 실제 프로젝트에서는 삭제하거나 주석처리하세요.
 */

// ==================== 예시 1: 기본 사용법 ====================

class UserServiceExample {
    private val log = CustomLogger(UserServiceExample::class.java)

    fun registerUser(email: String, password: String): Long {
        log.info("Starting user registration", mapOf(
            "email" to email
        ))

        try {
            // 사용자 등록 로직
            val userId = saveUser(email, password)

            log.info("User registered successfully", mapOf(
                "userId" to userId,
                "email" to email
            ))

            return userId
        } catch (e: Exception) {
            log.error("User registration failed", e, mapOf(
                "email" to email,
                "errorType" to e.javaClass.simpleName
            ))
            throw e
        }
    }

    private fun saveUser(email: String, password: String): Long {
        // DB 저장 로직
        return 12345L
    }
}

// ==================== 예시 2: 트랜잭션 로깅 ====================

class OrderServiceExample {
    private val log = CustomLogger(OrderServiceExample::class.java)

    fun createOrder(userId: Long, stockId: Long, quantity: Int, price: Long): Long {
        val startTime = System.currentTimeMillis()

        log.debug("Order creation started", mapOf(
            "userId" to userId,
            "stockId" to stockId,
            "quantity" to quantity,
            "price" to price
        ))

        try {
            // 1. 잔액 확인
            val balance = checkBalance(userId)
            log.debug("Balance checked", mapOf(
                "userId" to userId,
                "balance" to balance
            ))

            // 2. 주문 생성
            val orderId = saveOrder(userId, stockId, quantity, price)

            val duration = System.currentTimeMillis() - startTime
            log.info("Order created successfully", mapOf(
                "orderId" to orderId,
                "userId" to userId,
                "stockId" to stockId,
                "quantity" to quantity,
                "totalAmount" to quantity * price,
                "duration" to duration
            ))

            return orderId
        } catch (e: InsufficientBalanceException) {
            log.warn("Order creation failed: insufficient balance", e, mapOf(
                "userId" to userId,
                "requiredAmount" to quantity * price,
                "availableBalance" to checkBalance(userId)
            ))
            throw e
        } catch (e: Exception) {
            log.error("Order creation failed", e, mapOf(
                "userId" to userId,
                "stockId" to stockId
            ))
            throw e
        }
    }

    private fun checkBalance(userId: Long): Long = 1000000L
    private fun saveOrder(userId: Long, stockId: Long, quantity: Int, price: Long): Long = 54321L
}

class InsufficientBalanceException(message: String) : RuntimeException(message)

// ==================== 예시 3: 비즈니스 이벤트 로깅 ====================

class StockServiceExample {
    private val log = CustomLogger(StockServiceExample::class.java)

    fun listNewStock(companyName: String, sector: String, initialPrice: Long) {
        log.info("IPO event: New stock listed", mapOf(
            "eventType" to "IPO",
            "companyName" to companyName,
            "sector" to sector,
            "initialPrice" to initialPrice,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    fun delistStock(stockId: Long, reason: String) {
        log.warn("Stock delisted", null, mapOf(
            "eventType" to "DELISTING",
            "stockId" to stockId,
            "reason" to reason,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    fun updatePrice(stockId: Long, oldPrice: Long, newPrice: Long) {
        val changePercent = ((newPrice - oldPrice).toDouble() / oldPrice) * 100

        if (kotlin.math.abs(changePercent) > 10) {
            log.warn("Significant price change detected", null, mapOf(
                "stockId" to stockId,
                "oldPrice" to oldPrice,
                "newPrice" to newPrice,
                "changePercent" to String.format("%.2f", changePercent)
            ))
        } else {
            log.debug("Price updated", mapOf(
                "stockId" to stockId,
                "newPrice" to newPrice
            ))
        }
    }
}

// ==================== 예시 4: 외부 API 호출 로깅 ====================

class ExternalApiServiceExample {
    private val log = CustomLogger(ExternalApiServiceExample::class.java)

    fun callExternalApi(endpoint: String, params: Map<String, Any>): String {
        val startTime = System.currentTimeMillis()

        log.info("Calling external API", mapOf(
            "endpoint" to endpoint,
            "params" to params.toString()
        ))

        try {
            val response = performApiCall(endpoint, params)
            val duration = System.currentTimeMillis() - startTime

            log.info("External API call succeeded", mapOf(
                "endpoint" to endpoint,
                "duration" to duration,
                "responseLength" to response.length
            ))

            return response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime

            log.error("External API call failed", e, mapOf(
                "endpoint" to endpoint,
                "duration" to duration,
                "errorMessage" to e.message
            ))

            throw e
        }
    }

    private fun performApiCall(endpoint: String, params: Map<String, Any>): String {
        // 실제 API 호출
        return "response"
    }
}

// ==================== 예시 5: 스케줄러 작업 로깅 ====================

@Service
class ScheduledTaskExample {
    private val log = CustomLogger(ScheduledTaskExample::class.java)

    // @Scheduled(cron = "0 */30 * * * *")  // 30분마다
    fun generateNewInvestors() {
        val startTime = System.currentTimeMillis()

        log.info("Scheduled task started", mapOf(
            "taskName" to "generateNewInvestors"
        ))

        try {
            val count = createRandomInvestors()
            val duration = System.currentTimeMillis() - startTime

            log.info("Scheduled task completed", mapOf(
                "taskName" to "generateNewInvestors",
                "investorsCreated" to count,
                "duration" to duration
            ))
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime

            log.error("Scheduled task failed", e, mapOf(
                "taskName" to "generateNewInvestors",
                "duration" to duration
            ))
        }
    }

    private fun createRandomInvestors(): Int {
        // 투자자 생성 로직
        return 5
    }
}

// ==================== 예시 6: TraceId 활용 ====================

class TraceIdExample {
    private val log = CustomLogger(TraceIdExample::class.java)

    fun processRequest(requestId: String) {
        // TraceId 설정 (보통 필터나 인터셉터에서 수행)
        CustomLogger.setTraceId(requestId)

        try {
            log.info("Request processing started")

            step1()
            step2()
            step3()

            log.info("Request processing completed")
        } finally {
            // TraceId 정리
            CustomLogger.clearTraceId()
        }
    }

    private fun step1() {
        log.debug("Step 1 executed")
    }

    private fun step2() {
        log.debug("Step 2 executed")
    }

    private fun step3() {
        log.debug("Step 3 executed")
    }
}

// ==================== 예시 7: 성능 측정 로깅 ====================

class PerformanceLoggingExample {
    private val log = CustomLogger(PerformanceLoggingExample::class.java)

    fun complexOperation(dataSize: Int) {
        val startTime = System.currentTimeMillis()

        log.debug("Complex operation started", mapOf(
            "dataSize" to dataSize
        ))

        // 복잡한 작업
        val result = performComplexCalculation(dataSize)

        val duration = System.currentTimeMillis() - startTime

        // 성능 임계값 체크
        when {
            duration > 5000 -> log.error("Operation too slow", null, mapOf(
                "duration" to duration,
                "dataSize" to dataSize,
                "threshold" to 5000
            ))
            duration > 2000 -> log.warn("Operation slow", null, mapOf(
                "duration" to duration,
                "dataSize" to dataSize,
                "threshold" to 2000
            ))
            else -> log.info("Operation completed", mapOf(
                "duration" to duration,
                "dataSize" to dataSize,
                "result" to result
            ))
        }
    }

    private fun performComplexCalculation(dataSize: Int): Int {
        Thread.sleep(100) // 시뮬레이션
        return dataSize * 2
    }
}

// ==================== MongoDB 쿼리 예시 (주석) ====================

/*
MongoDB에서 로그 조회 예시:

1. 특정 사용자의 모든 활동 추적:
db.application_logs.find({ 
  "metadata.userId": 12345 
}).sort({ timestamp: -1 })

2. TraceId로 전체 요청 흐름 추적:
db.application_logs.find({ 
  traceId: "abc123" 
}).sort({ timestamp: 1 })

3. 느린 작업 찾기:
db.application_logs.find({ 
  "metadata.duration": { $gt: 2000 } 
}).sort({ "metadata.duration": -1 })

4. 특정 시간대의 에러:
db.application_logs.find({
  level: "ERROR",
  timestamp: {
    $gte: ISODate("2025-01-15T10:00:00Z"),
    $lt: ISODate("2025-01-15T11:00:00Z")
  }
})

5. 서비스별 에러 통계:
db.application_logs.aggregate([
  { $match: { level: "ERROR" } },
  { $group: { _id: "$serviceName", count: { $sum: 1 } } },
  { $sort: { count: -1 } }
])
*/
