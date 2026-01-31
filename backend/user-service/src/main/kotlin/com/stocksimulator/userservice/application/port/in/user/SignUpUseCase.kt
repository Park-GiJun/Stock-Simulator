package com.stocksimulator.userservice.application.port.`in`.user

import com.stocksimulator.userservice.application.dto.command.user.SignUpCommand
import com.stocksimulator.userservice.application.dto.result.user.SingUpResult

interface SignUpUseCase {
    fun signUp(command: SignUpCommand): SingUpResult
}