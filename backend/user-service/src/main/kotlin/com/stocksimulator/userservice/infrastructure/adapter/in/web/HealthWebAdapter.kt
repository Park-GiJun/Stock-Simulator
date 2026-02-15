package com.stocksimulator.userservice.infrastructure.adapter.`in`.web

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    private val log = LoggerFactory.getLogger(HealthController::class.java)

    @GetMapping
    fun healthCheck(): ResponseEntity<HealthResponse> {
        log.info("🏥 Health check requested at {}", LocalDateTime.now())
        
        val response = HealthResponse(
            status = "UP",
            service = "user-service",
            timestamp = LocalDateTime.now().toString(),
            message = "UserModel Service is running!"
        )
        
        log.info("✅ Health check response: status={}, service={}", response.status, response.service)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        log.info("🏓 Ping requested at {}", LocalDateTime.now())
        return ResponseEntity.ok("pong")
    }

    data class HealthResponse(
        val status: String,
        val service: String,
        val timestamp: String,
        val message: String
    )
}
