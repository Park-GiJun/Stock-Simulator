package com.stocksimulator.userservice.application.dto.result.user

data class SingUpResult(
    val userId: Long,
    val email: String,
    val username: String
)
