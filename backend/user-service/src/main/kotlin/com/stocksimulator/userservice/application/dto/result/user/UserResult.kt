package com.stocksimulator.userservice.application.dto.result.user

import com.stocksimulator.userservice.domain.enums.UserRole

data class UserResult(
    val userId: Long,
    val email: String,
    val username: String,
    val role: UserRole
)
