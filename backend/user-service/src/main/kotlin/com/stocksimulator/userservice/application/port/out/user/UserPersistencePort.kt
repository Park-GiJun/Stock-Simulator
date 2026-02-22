package com.stocksimulator.userservice.application.port.out.user

import com.stocksimulator.userservice.domain.model.UserModel

/**
 * User 영속성 Port (Driven Port)
 * - User 도메인 전용 저장소 인터페이스
 */
interface UserPersistencePort {
    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): UserModel?
    
    /**
     * 닉네임으로 사용자 조회
     */
    fun findByUsername(username: String): UserModel?
    
    /**
     * 사용자 ID로 조회
     */
    fun findById(userId: Long): UserModel?
    
    /**
     * 사용자 저장
     */
    fun save(user: UserModel): UserModel
    
    /**
     * 사용자 업데이트
     */
    fun update(user: UserModel): UserModel
}