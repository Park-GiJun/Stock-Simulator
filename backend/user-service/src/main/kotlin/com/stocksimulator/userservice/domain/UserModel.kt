package com.stocksimulator.userservice.domain

import com.stocksimulator.userservice.domain.enums.UserRole
import java.time.Instant

/**
 * User Domain Model (순수 비즈니스 로직)
 * - ID는 Long? (IDENTITY 전략)
 * - 생성 시 null, 영속화 후 값 부여
 */
data class UserModel(
    val userId: Long? = null,
    val email: String,
    val username: String,
    val password: String,
    val role: UserRole = UserRole.ROLE_USER,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수입니다." }
        require(username.isNotBlank()) { "사용자명은 필수입니다." }
        require(username.length in 2..20) { "사용자명은 2~20자여야 합니다." }
    }

    companion object {
        /**
         * 신규 사용자 생성 (ID 없음)
         */
        fun create(
            email: String,
            username: String,
            password: String,
            role: UserRole = UserRole.ROLE_USER
        ): UserModel {
            return UserModel(
                userId = null,
                email = email,
                username = username,
                password = password,
                role = role,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }

        /**
         * 기존 사용자 재구성 (DB 조회 후)
         */
        fun of(
            userId: Long,
            email: String,
            username: String,
            password: String,
            role: UserRole,
            createdAt: Instant,
            updatedAt: Instant
        ): UserModel {
            return UserModel(
                userId = userId,
                email = email,
                username = username,
                password = password,
                role = role,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    /**
     * 영속화 후 ID 부여
     */
    fun withId(id: Long): UserModel {
        return this.copy(userId = id)
    }

    /**
     * 닉네임 변경
     */
    fun updateUsername(newUsername: String): UserModel {
        require(newUsername.length in 2..20) { "사용자명은 2~20자여야 합니다." }
        return this.copy(
            username = newUsername,
            updatedAt = Instant.now()
        )
    }

    /**
     * 신규 생성 여부
     */
    fun isNew(): Boolean = userId == null
}
