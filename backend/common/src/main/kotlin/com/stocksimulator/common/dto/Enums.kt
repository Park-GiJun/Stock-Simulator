package com.stocksimulator.common.dto

/**
 * 공통 도메인 Enum 정의
 */

// 주식 섹터
enum class Sector(val displayName: String) {
    IT("IT"),
    AGRICULTURE("농경"),
    MANUFACTURING("제조업"),
    SERVICE("서비스업"),
    REAL_ESTATE("부동산"),
    LUXURY("럭셔리"),
    FOOD("식료")
}

// 시가총액 등급
enum class MarketCapGrade(val displayName: String, val minMarketCap: Long, val maxMarketCap: Long) {
    SMALL("소형주", 0L, 10_000_000_000L),                    // ~100억
    MID("중형주", 10_000_000_000L, 100_000_000_000L),       // ~1,000억
    LARGE("대형주", 100_000_000_000L, 1_000_000_000_000L)   // ~1조
}

// 주문 유형
enum class OrderType(val displayName: String) {
    BUY("매수"),
    SELL("매도")
}

// 주문 상태
enum class OrderStatus(val displayName: String) {
    PENDING("대기중"),
    PARTIALLY_FILLED("부분체결"),
    FILLED("체결완료"),
    CANCELLED("취소됨"),
    REJECTED("거부됨")
}

// 이벤트 레벨
enum class EventLevel(val displayName: String) {
    SOCIETY("사회"),
    INDUSTRY("산업"),
    COMPANY("기업")
}

// 감정 (이벤트 영향)
enum class Sentiment(val multiplier: Int) {
    POSITIVE(1),
    NEGATIVE(-1),
    NEUTRAL(0)
}

// 투자 성향
enum class InvestmentStyle(val displayName: String) {
    AGGRESSIVE("공격형"),
    STABLE("안정형"),
    VALUE("가치투자형"),
    RANDOM("랜덤형")
}

// 거래 빈도
enum class TradingFrequency(val displayName: String) {
    HIGH("높음"),
    MEDIUM("보통"),
    LOW("낮음")
}

// 주식 상태
enum class StockStatus(val displayName: String) {
    LISTED("상장"),
    SUSPENDED("거래정지"),
    DELISTED("상장폐지")
}

// 투자자 유형
enum class InvestorType(val displayName: String) {
    INDIVIDUAL("개인"),
    INSTITUTION("기관")
}

// 랭킹 유형
enum class RankingType(val displayName: String) {
    RETURN_RATE("수익률"),
    TOTAL_ASSET("총자산"),
    TRADE_VOLUME("거래량")
}

enum class OrderKind(val displayName: String) {
    LIMIT("지정가"),
    MARKET("시장가")
}

enum class InstitutionType(val displayName: String) {
    INSTITUTIONAL_INVESTOR("기관투자자"),
    FOREIGN_INVESTOR("외인"),
    PENSION_FUNDS("연기금"),
    ASSET_MANAGEMENT("자산운용사")

}
