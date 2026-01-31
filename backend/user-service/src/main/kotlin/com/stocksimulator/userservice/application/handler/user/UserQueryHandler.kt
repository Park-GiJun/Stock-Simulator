package com.stocksimulator.userservice.application.handler.user

import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.common.exception.InvalidInputException
import com.stocksimulator.common.exception.ResourceNotFoundException
import com.stocksimulator.common.logging.CustomLogger
import com.stocksimulator.userservice.application.dto.command.user.LoginCommand
import com.stocksimulator.userservice.application.dto.result.user.LoginResult
import com.stocksimulator.userservice.application.dto.result.user.UserResult
import com.stocksimulator.userservice.application.port.`in`.user.GetCurrentUserUseCase
import com.stocksimulator.userservice.application.port.`in`.user.LoginUseCase
import com.stocksimulator.userservice.application.port.out.user.UserPersistencePort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserQueryHandler(
    private val userPersistencePort: UserPersistencePort,
    private val passwordEncoder: PasswordEncoder
) : LoginUseCase, GetCurrentUserUseCase {

    private val log = CustomLogger(UserQueryHandler::class.java)

    /**
     * 로그인 처리
     * 1. 이메일로 사용자 조회
     * 2. 비밀번호 검증
     * 3. LoginResult 반환 (세션은 Web Layer에서 처리)
     */
    override fun login(command: LoginCommand): LoginResult {
        log.info("로그인 시도", mapOf("email" to command.email))

        // 1. 이메일로 사용자 조회
        val user = userPersistencePort.findByEmail(command.email)
            ?: run {
                log.warn("사용자를 찾을 수 없음", metadata = mapOf("email" to command.email))
                throw ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "이메일 또는 비밀번호가 일치하지 않습니다")
            }

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(command.password, user.password)) {
            log.warn("비밀번호 불일치", metadata = mapOf("userId" to user.userId))
            throw InvalidInputException(ErrorCode.INVALID_PASSWORD, "이메일 또는 비밀번호가 일치하지 않습니다")
        }

        log.info("로그인 성공", mapOf("userId" to user.userId, "email" to user.email))

        // 3. LoginResult 반환
        return LoginResult(
            userId = user.userId ?: throw IllegalStateException("User ID must not be null"),
            email = user.email,
            username = user.username,
            role = user.role
        )
    }

    /**
     * 현재 사용자 정보 조회
     */
    override fun getCurrentUser(userId: Long): UserResult {
        log.info("사용자 정보 조회", mapOf("userId" to userId))

        val user = userPersistencePort.findById(userId)
            ?: run {
                log.warn("사용자를 찾을 수 없음", metadata = mapOf("userId" to userId))
                throw ResourceNotFoundException(ErrorCode.USER_NOT_FOUND)
            }

        return UserResult(
            userId = user.userId ?: throw IllegalStateException("User ID must not be null"),
            email = user.email,
            username = user.username,
            role = user.role
        )
    }
}
