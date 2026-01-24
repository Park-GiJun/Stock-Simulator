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
 * - 현실 3시간 = 게임 12시간 (장 마감)
 * - 현실 6시간 = 게임 1일
 */
object GameTimeUtil {

    // 가속 배율 (현실 1분 = 게임 4분)
    const val TIME_SCALE = 4

    // 게임 장 시간 (게임 시간 기준)
    const val MARKET_OPEN_HOUR = 9   // 09:00
    const val MARKET_CLOSE_HOUR = 21 // 21:00
    const val MARKET_HOURS = 12

    // 현실 시간으로 장 운영 시간 (3시간)
    val REAL_MARKET_DURATION: Duration = Duration.ofHours(3)

    // 시즌 시작 기준 시간 (서버 시작 또는 시즌 시작 시 설정)
    @Volatile
    private var seasonStartRealTime: Instant = Instant.now()

    @Volatile
    private var seasonStartGameTime: LocalDateTime = LocalDateTime.of(2025, 1, 1, MARKET_OPEN_HOUR, 0)

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
     * 현재 장이 열려있는지 확인
     */
    fun isMarketOpen(): Boolean {
        val gameTime = getCurrentGameTime()
        val hour = gameTime.hour
        return hour in MARKET_OPEN_HOUR until MARKET_CLOSE_HOUR
    }

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
     * 다음 장 시작까지 남은 현실 시간
     */
    fun getTimeUntilMarketOpen(): Duration {
        if (isMarketOpen()) return Duration.ZERO

        val gameTime = getCurrentGameTime()
        val hour = gameTime.hour

        val hoursUntilOpen = if (hour >= MARKET_CLOSE_HOUR) {
            // 장 마감 후: 다음날 09:00까지
            24 - hour + MARKET_OPEN_HOUR
        } else {
            // 장 시작 전: 당일 09:00까지
            MARKET_OPEN_HOUR - hour
        }

        val gameMinutesUntilOpen = hoursUntilOpen * 60L - gameTime.minute
        return Duration.ofMinutes(gameMinutesUntilOpen / TIME_SCALE)
    }

    /**
     * 장 마감까지 남은 현실 시간
     */
    fun getTimeUntilMarketClose(): Duration {
        if (!isMarketOpen()) return Duration.ZERO

        val gameTime = getCurrentGameTime()
        val gameMinutesUntilClose = (MARKET_CLOSE_HOUR - gameTime.hour) * 60L - gameTime.minute
        return Duration.ofMinutes(gameMinutesUntilClose / TIME_SCALE)
    }
}
