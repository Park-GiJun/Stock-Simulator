package com.stocksimulator.userservice.adapter.out.persistence.user.entity

import com.stocksimulator.userservice.domain.UserModel
import com.stocksimulator.userservice.domain.enums.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "users",
    schema = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email")
    ]
)
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false, unique = true, length = 100)
    var username: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val role: UserRole = UserRole.ROLE_USER,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    protected constructor() : this(
        userId = null,
        email = "",
        password = "",
        username = "",
        role = UserRole.ROLE_USER,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserJpaEntity

        return userId != null && userId == other.userId
    }

    override fun hashCode(): Int {
        return userId?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "UserJpaEntity(userId=$userId, email='$email', username='$username', role=$role)"
    }

    fun toDomain(): UserModel {
        return UserModel.of(
            userId = userId ?: throw IllegalStateException("User ID must not be null"),
            email = email,
            username = username,
            password = password,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(user: UserModel, encodedPassword: String): UserJpaEntity {
            return UserJpaEntity(
                userId = user.userId,
                email = user.email,
                password = encodedPassword,
                username = user.username,
                role = user.role,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}