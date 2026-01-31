package com.stocksimulator.userservice.application.port.`in`.user

import com.stocksimulator.userservice.application.dto.command.user.LoginCommand
import com.stocksimulator.userservice.application.dto.result.user.LoginResult

interface LoginUseCase {
    fun login(command: LoginCommand): LoginResult
}
