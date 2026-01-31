package com.stocksimulator.userservice.application.dto.command.user

data class LoginCommand(
    val email: String,
    val password: String
)
