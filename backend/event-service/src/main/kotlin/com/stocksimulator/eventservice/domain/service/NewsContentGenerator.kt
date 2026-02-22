package com.stocksimulator.eventservice.domain.service

import org.springframework.stereotype.Service

@Service
class NewsContentGenerator {

    fun generateIpoHeadline(stockName: String, sector: String): String {
        return "[속보] $stockName, 오늘 증시에 신규 상장"
    }

    fun generateIpoContent(stockName: String, sector: String, basePrice: Long, totalShares: Long, marketCapGrade: String): String {
        val marketCapLabel = when (marketCapGrade) {
            "LARGE" -> "대형주"
            "MID" -> "중형주"
            else -> "소형주"
        }
        return "$sector 섹터의 $stockName 이(가) 오늘 증권시장에 신규 상장했다. " +
                "공모가는 ${basePrice}원이며, 총 발행 주식 수는 ${totalShares}주로, $marketCapLabel 로 분류된다. " +
                "시장 관계자들은 해당 종목의 향후 실적에 대해 관심을 보이고 있다."
    }

    fun generateIpoSummary(stockName: String, sector: String): String {
        return "$stockName($sector) 신규 상장"
    }

    fun generateDelistingHeadline(stockName: String): String {
        return "[속보] $stockName, 상장폐지 결정"
    }

    fun generateDelistingContent(stockName: String, reason: String, finalPrice: Long): String {
        return "$stockName 이(가) 상장폐지 결정을 받았다. " +
                "폐지 사유는 '$reason'이며, 최종 거래가는 ${finalPrice}원이다. " +
                "해당 종목을 보유한 투자자들은 주의가 필요하다."
    }

    fun generateDelistingSummary(stockName: String): String {
        return "$stockName 상장폐지 결정"
    }
}
