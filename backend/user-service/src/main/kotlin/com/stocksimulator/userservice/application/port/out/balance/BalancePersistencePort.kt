package com.stocksimulator.userservice.application.port.out.balance

import com.stocksimulator.userservice.domain.model.BalanceModel

/**
 * Balance 영속성 Port (Driven Port)
 * - Balance 도메인 전용 저장소 인터페이스
 */
interface BalancePersistencePort {
    /**
     * 잔고 저장
     */
    fun save(balance: BalanceModel): BalanceModel
    
    /**
     * 사용자 ID로 잔고 조회
     */
    fun findByUserId(userId: Long): BalanceModel?
    
    /**
     * 잔고 업데이트
     */
    fun update(balance: BalanceModel): BalanceModel
}
