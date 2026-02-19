package com.stocksimulator.userservice.application.handler.user

import com.stocksimulator.common.exception.DuplicateResourceException
import com.stocksimulator.common.exception.ErrorCode
import org.slf4j.LoggerFactory
import com.stocksimulator.userservice.application.dto.command.user.SignUpCommand
import com.stocksimulator.userservice.application.dto.result.user.SingUpResult
import com.stocksimulator.userservice.application.port.`in`.user.SignUpUseCase
import com.stocksimulator.userservice.application.port.out.balance.BalancePersistencePort
import com.stocksimulator.userservice.application.port.out.user.UserPersistencePort
import com.stocksimulator.userservice.domain.model.BalanceModel
import com.stocksimulator.userservice.domain.model.UserModel
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCommandHandler(
    private val userPersistencePort: UserPersistencePort,
    private val balancePersistencePort: BalancePersistencePort,
    private val passwordEncoder: PasswordEncoder
) : SignUpUseCase {

    private val log = LoggerFactory.getLogger(UserCommandHandler::class.java)

    /**
     * 회원가입 처리
     * 1. 이메일/닉네임 중복 검증
     * 2. 비밀번호 암호화 (BCrypt)
     * 3. User 생성 및 저장
     * 4. 초기 잔고 생성 (500만원)
     */
    @Transactional
    override fun signUp(command: SignUpCommand): SingUpResult {
        log.info("회원가입 시작: email={}, username={}", command.email, command.username)

        // 1. 이메일 중복 검증
        userPersistencePort.findByEmail(command.email)?.let {
            log.warn("이메일 중복: email={}", command.email)
            throw DuplicateResourceException(ErrorCode.DUPLICATE_EMAIL)
        }

        // 2. 닉네임 중복 검증
        userPersistencePort.findByUsername(command.username)?.let {
            log.warn("닉네임 중복: username={}", command.username)
            throw DuplicateResourceException(ErrorCode.DUPLICATE_NICKNAME)
        }

        // 3. 비밀번호 암호화
        val encryptedPassword: String = passwordEncoder.encode(command.password) ?: 
            throw IllegalStateException("Password encoding failed")
        log.debug("비밀번호 암호화 완료")

        // 4. User 도메인 생성
        val newUser = UserModel.create(
            email = command.email,
            username = command.username,
            password = encryptedPassword
        )

        // 5. User 저장
        val savedUser = userPersistencePort.save(newUser)
        log.info("사용자 저장 완료", mapOf("userId" to savedUser.userId, "email" to savedUser.email))

        // 6. 초기 잔고 생성 (500만원)
        val initialBalance = BalanceModel.create(userId = savedUser.userId!!)
        val savedBalance = balancePersistencePort.save(initialBalance)
        log.info(
            "초기 잔고 생성 완료",
            mapOf(
                "userId" to savedUser.userId,
                "balanceId" to savedBalance.balanceId,
                "cash" to savedBalance.cash
            )
        )

        // 7. Result 반환
        return SingUpResult(
            userId = savedUser.userId ?: throw IllegalStateException("User ID must not be null after save"),
            email = savedUser.email,
            username = savedUser.username
        )
    }
}
