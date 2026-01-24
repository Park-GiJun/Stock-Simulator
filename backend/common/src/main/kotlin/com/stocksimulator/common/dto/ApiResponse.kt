package com.stocksimulator.common.dto

import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        fun <T> success(message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, message = message)

        fun <T> error(message: String, errorCode: String? = null): ApiResponse<T> =
            ApiResponse(success = false, message = message, errorCode = errorCode)

        fun <T> created(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)
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
    val last: Boolean,
    val empty: Boolean = content.isEmpty()
) {
    companion object {
        fun <T> of(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long
        ): PageResponse<T> {
            val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
            return PageResponse(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                first = page == 0,
                last = page >= totalPages - 1
            )
        }
    }
}

@Serializable
data class SliceResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val first: Boolean,
    val empty: Boolean = content.isEmpty()
)

fun <T> ApiResponse<T>.toResponseEntity(): ResponseEntity<ApiResponse<T>> {
    return if (this.success) {
        ResponseEntity.ok(this)
    } else {
        ResponseEntity.badRequest().body(this)
    }
}

fun <T> ApiResponse<T>.toResponseEntity(status: HttpStatus): ResponseEntity<ApiResponse<T>> {
    return ResponseEntity.status(status).body(this)
}

fun <T> ApiResponse<T>.toCreatedResponseEntity(): ResponseEntity<ApiResponse<T>> {
    return ResponseEntity.status(HttpStatus.CREATED).body(this)
}
