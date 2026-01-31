package com.stocksimulator.userservice.application.port.`in`.user

import com.stocksimulator.userservice.application.dto.result.user.UserResult

interface GetCurrentUserUseCase {
    fun getCurrentUser(userId: Long): UserResult
}
