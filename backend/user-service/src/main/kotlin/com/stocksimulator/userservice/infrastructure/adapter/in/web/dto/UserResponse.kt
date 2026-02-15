package com.stocksimulator.userservice.infrastructure.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 정보 응답")
data class UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    val userId: Long,
    
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
    
    @Schema(description = "닉네임", example = "테스트유저")
    val username: String,
    
    @Schema(description = "권한", example = "ROLE_USER")
    val role: String
)
