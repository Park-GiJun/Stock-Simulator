package com.stocksimulator.userservice.adapter.`in`.web

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    private val logger = LoggerFactory.getLogger(HealthController::class.java)

    @GetMapping
    fun healthCheck(): ResponseEntity<HealthResponse> {
        logger.info("🏥 Health check requested at ${LocalDateTime.now()}")
        
        val response = HealthResponse(
            status = "UP",
            service = "user-service",
            timestamp = LocalDateTime.now().toString(),
            message = "UserModel Service is running!"
        )
        
        logger.info("✅ Health check response: $response")
        return ResponseEntity.ok(response)
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        logger.info("🏓 Ping requested at ${LocalDateTime.now()}")
        return ResponseEntity.ok("pong")
    }

    data class HealthResponse(
        val status: String,
        val service: String,
        val timestamp: String,
        val message: String
    )
}
