package com.stocksimulator.common.exception

import com.stocksimulator.common.dto.ApiResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn { "BusinessException: [${ex.errorCode.code}] ${ex.message}" }
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(ApiResponse.error(ex.message, ex.errorCode.code))
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn { "ResourceNotFoundException: [${ex.errorCode.code}] ${ex.message}" }
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(ApiResponse.error(ex.message, ex.errorCode.code))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn { "UnauthorizedException: [${ex.errorCode.code}] ${ex.message}" }
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(ApiResponse.error(ex.message, ex.errorCode.code))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")
        logger.warn { "ValidationException: $errors" }
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(errors, ErrorCode.VALIDATION_ERROR.code))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn { "IllegalArgumentException: ${ex.message}" }
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(ex.message ?: "잘못된 인자입니다", ErrorCode.INVALID_INPUT.code))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(ex) { "Unexpected error occurred" }
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.message, ErrorCode.INTERNAL_ERROR.code))
    }
}
