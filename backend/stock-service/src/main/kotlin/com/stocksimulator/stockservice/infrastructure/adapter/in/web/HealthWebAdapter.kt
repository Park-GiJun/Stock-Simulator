package com.stocksimulator.stockservice.infrastructure.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/health")
class HealthWebAdapter {

    @GetMapping
    fun healthCheck(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
            HealthResponse(
                status = "UP",
                service = "stock-service",
                timestamp = LocalDateTime.now().toString(),
                message = "Stock Service is running!"
            )
        )
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok("pong")
    }

    data class HealthResponse(
        val status: String,
        val service: String,
        val timestamp: String,
        val message: String
    )
}
