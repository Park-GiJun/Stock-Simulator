package com.stocksimulator.common.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 게임 시간 관리 유틸리티
 *
 * 현실 시간:게임 시간 = 1:4
 * - 현실 15분 = 게임 1시간
 * - 현실 1시간 = 게임 4시간
 * - 현실 6시간 = 게임 1일
 *
 * 24시간 연속 운영 (장 시작/마감 없음)
 */
object GameTimeUtil {

    // 가속 배율 (현실 1분 = 게임 4분)
    const val TIME_SCALE = 4

    // 시즌 시작 기준 시간 (서버 시작 또는 시즌 시작 시 설정)
    @Volatile
    private var seasonStartRealTime: Instant = Instant.now()

    @Volatile
    private var seasonStartGameTime: LocalDateTime = LocalDateTime.of(2025, 1, 1, 0, 0)

    /**
     * 시즌 시작 시간 설정
     */
    fun setSeasonStart(realTime: Instant, gameTime: LocalDateTime) {
        seasonStartRealTime = realTime
        seasonStartGameTime = gameTime
    }

    /**
     * 현재 게임 시간 조회
     */
    fun getCurrentGameTime(): LocalDateTime {
        val realElapsed = Duration.between(seasonStartRealTime, Instant.now())
        val gameElapsedMinutes = realElapsed.toMinutes() * TIME_SCALE
        return seasonStartGameTime.plusMinutes(gameElapsedMinutes)
    }

    /**
     * 장은 항상 열려있음 (24시간 운영)
     */
    fun isMarketOpen(): Boolean = true

    /**
     * 현실 시간 → 게임 시간 변환
     */
    fun realToGameDuration(realDuration: Duration): Duration {
        return Duration.ofMinutes(realDuration.toMinutes() * TIME_SCALE)
    }

    /**
     * 게임 시간 → 현실 시간 변환
     */
    fun gameToRealDuration(gameDuration: Duration): Duration {
        return Duration.ofMinutes(gameDuration.toMinutes() / TIME_SCALE)
    }

    /**
     * 게임 1일이 현실로 몇 시간인지
     */
    fun getGameDayInRealHours(): Long = 6L // 현실 6시간 = 게임 1일

    /**
     * 게임 1주일이 현실로 몇 시간인지
     */
    fun getGameWeekInRealHours(): Long = 42L // 현실 42시간 = 게임 1주일

    /**
     * 24시간 운영이므로 항상 ZERO
     */
    fun getTimeUntilMarketOpen(): Duration = Duration.ZERO

    /**
     * 24시간 운영이므로 장 마감 없음
     */
    fun getTimeUntilMarketClose(): Duration = Duration.ZERO
}
