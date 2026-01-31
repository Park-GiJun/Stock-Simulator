package com.stocksimulator.userservice.adapter.`in`.web

import com.stocksimulator.common.logging.CustomLogger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    private val log = CustomLogger(HealthController::class.java)

    @GetMapping
    fun healthCheck(): ResponseEntity<HealthResponse> {
        log.info("🏥 Health check requested", mapOf("timestamp" to LocalDateTime.now()))
        
        val response = HealthResponse(
            status = "UP",
            service = "user-service",
            timestamp = LocalDateTime.now().toString(),
            message = "UserModel Service is running!"
        )
        
        log.info("✅ Health check response", mapOf("status" to response.status, "service" to response.service))
        return ResponseEntity.ok(response)
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        log.info("🏓 Ping requested", mapOf("timestamp" to LocalDateTime.now()))
        return ResponseEntity.ok("pong")
    }

    data class HealthResponse(
        val status: String,
        val service: String,
        val timestamp: String,
        val message: String
    )
}
