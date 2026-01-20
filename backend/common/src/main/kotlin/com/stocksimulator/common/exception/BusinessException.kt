package com.stocksimulator.common.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)

enum class ErrorCode(val code: String, val message: String) {
    // Common
    INVALID_INPUT("C001", "잘못된 입력입니다"),
    RESOURCE_NOT_FOUND("C002", "리소스를 찾을 수 없습니다"),
    INTERNAL_ERROR("C003", "서버 내부 오류가 발생했습니다"),

    // Auth
    UNAUTHORIZED("A001", "인증이 필요합니다"),
    FORBIDDEN("A002", "접근 권한이 없습니다"),
    INVALID_TOKEN("A003", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN("A004", "만료된 토큰입니다"),

    // User
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("U002", "이미 사용 중인 이메일입니다"),
    DUPLICATE_NICKNAME("U003", "이미 사용 중인 닉네임입니다"),
    INVALID_PASSWORD("U004", "비밀번호가 일치하지 않습니다"),

    // Stock
    STOCK_NOT_FOUND("S001", "종목을 찾을 수 없습니다"),
    MARKET_CLOSED("S002", "장이 마감되었습니다"),

    // Trading
    INSUFFICIENT_BALANCE("T001", "잔액이 부족합니다"),
    INSUFFICIENT_STOCK("T002", "보유 수량이 부족합니다"),
    INVALID_ORDER_QUANTITY("T003", "주문 수량이 유효하지 않습니다"),
    INVALID_ORDER_PRICE("T004", "주문 가격이 유효하지 않습니다"),
    ORDER_NOT_FOUND("T005", "주문을 찾을 수 없습니다"),

    // Season
    SEASON_NOT_FOUND("R001", "시즌을 찾을 수 없습니다"),
    SEASON_NOT_ACTIVE("R002", "활성화된 시즌이 없습니다"),
}
