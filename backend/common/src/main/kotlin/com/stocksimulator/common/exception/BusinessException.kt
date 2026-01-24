package com.stocksimulator.common.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)

// 특화된 예외 클래스들
class ResourceNotFoundException(
    errorCode: ErrorCode = ErrorCode.RESOURCE_NOT_FOUND,
    message: String = errorCode.message
) : BusinessException(errorCode, message)

class UnauthorizedException(
    errorCode: ErrorCode = ErrorCode.UNAUTHORIZED,
    message: String = errorCode.message
) : BusinessException(errorCode, message)

class ForbiddenException(
    errorCode: ErrorCode = ErrorCode.FORBIDDEN,
    message: String = errorCode.message
) : BusinessException(errorCode, message)

class InvalidInputException(
    errorCode: ErrorCode = ErrorCode.INVALID_INPUT,
    message: String = errorCode.message
) : BusinessException(errorCode, message)

class DuplicateResourceException(
    errorCode: ErrorCode,
    message: String = errorCode.message
) : BusinessException(errorCode, message)

class InsufficientResourceException(
    errorCode: ErrorCode,
    message: String = errorCode.message
) : BusinessException(errorCode, message)

enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    // Common (C0xx)
    INVALID_INPUT("C001", "잘못된 입력입니다", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("C002", "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR("C003", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("C004", "유효성 검사에 실패했습니다", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("C005", "지원하지 않는 HTTP 메소드입니다", HttpStatus.METHOD_NOT_ALLOWED),
    SERVICE_UNAVAILABLE("C006", "서비스를 일시적으로 사용할 수 없습니다", HttpStatus.SERVICE_UNAVAILABLE),

    // Auth (A0xx)
    UNAUTHORIZED("A001", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("A002", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("A003", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("A004", "만료된 토큰입니다", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("A005", "토큰이 없습니다", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("A006", "리프레시 토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),

    // User (U0xx)
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("U002", "이미 사용 중인 이메일입니다", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("U003", "이미 사용 중인 닉네임입니다", HttpStatus.CONFLICT),
    INVALID_PASSWORD("U004", "비밀번호가 일치하지 않습니다", HttpStatus.BAD_REQUEST),
    USER_DISABLED("U005", "비활성화된 계정입니다", HttpStatus.FORBIDDEN),

    // Stock (S0xx)
    STOCK_NOT_FOUND("S001", "종목을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MARKET_CLOSED("S002", "장이 마감되었습니다", HttpStatus.BAD_REQUEST),
    INVALID_STOCK_STATUS("S003", "유효하지 않은 종목 상태입니다", HttpStatus.BAD_REQUEST),
    PRICE_NOT_AVAILABLE("S004", "시세 정보를 조회할 수 없습니다", HttpStatus.SERVICE_UNAVAILABLE),

    // Trading (T0xx)
    INSUFFICIENT_BALANCE("T001", "잔액이 부족합니다", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("T002", "보유 수량이 부족합니다", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_QUANTITY("T003", "주문 수량이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_PRICE("T004", "주문 가격이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("T005", "주문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_CANCELLED("T006", "이미 취소된 주문입니다", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_MATCHED("T007", "이미 체결된 주문입니다", HttpStatus.BAD_REQUEST),
    TRADE_NOT_FOUND("T008", "거래 내역을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // Event (E0xx)
    EVENT_NOT_FOUND("E001", "이벤트를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_EVENT_TYPE("E002", "유효하지 않은 이벤트 유형입니다", HttpStatus.BAD_REQUEST),

    // News (N0xx)
    NEWS_NOT_FOUND("N001", "뉴스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // Season (R0xx)
    SEASON_NOT_FOUND("R001", "시즌을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    SEASON_NOT_ACTIVE("R002", "활성화된 시즌이 없습니다", HttpStatus.BAD_REQUEST),
    SEASON_ALREADY_ENDED("R003", "이미 종료된 시즌입니다", HttpStatus.BAD_REQUEST),
    RANKING_NOT_FOUND("R004", "랭킹 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // Scheduler (SC0xx)
    SCHEDULER_NOT_FOUND("SC001", "스케줄러를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    NPC_NOT_FOUND("SC002", "NPC를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INSTITUTION_NOT_FOUND("SC003", "기관 투자자를 찾을 수 없습니다", HttpStatus.NOT_FOUND)
}
