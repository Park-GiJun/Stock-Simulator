package com.stocksimulator.userservice.application.dto.command.user

data class SignUpCommand(
    val email: String,
    val username: String,
    val password: String
)
