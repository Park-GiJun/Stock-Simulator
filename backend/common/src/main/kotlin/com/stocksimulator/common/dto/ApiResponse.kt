package com.stocksimulator.common.dto

// Re-export from shared module for backward compatibility
typealias ApiResponse<T> = com.stocksimulator.shared.dto.ApiResponse<T>
typealias PageResponse<T> = com.stocksimulator.shared.dto.PageResponse<T>

// JVM-specific extension: Convert to Spring ResponseEntity
import org.springframework.http.ResponseEntity

fun <T> ApiResponse<T>.toResponseEntity(): ResponseEntity<ApiResponse<T>> {
    return if (this.success) {
        ResponseEntity.ok(this)
    } else {
        ResponseEntity.badRequest().body(this)
    }
}
