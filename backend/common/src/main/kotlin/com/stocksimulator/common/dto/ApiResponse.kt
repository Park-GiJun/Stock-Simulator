package com.stocksimulator.common.dto

import kotlinx.serialization.Serializable
import org.springframework.http.ResponseEntity

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        fun <T> error(message: String, errorCode: String? = null): ApiResponse<T> =
            ApiResponse(success = false, message = message, errorCode = errorCode)
    }
}

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

fun <T> ApiResponse<T>.toResponseEntity(): ResponseEntity<ApiResponse<T>> {
    return if (this.success) {
        ResponseEntity.ok(this)
    } else {
        ResponseEntity.badRequest().body(this)
    }
}
