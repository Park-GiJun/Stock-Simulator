package com.stocksimulator.eventservice.domain.service

import org.springframework.stereotype.Service

@Service
class NewsContentGenerator {

    fun generateIpoHeadline(stockName: String, sector: String): String {
        return "[속보] $stockName, 오늘 증시에 신규 상장"
    }

    fun generateIpoContent(stockName: String, sector: String, basePrice: Long, totalShares: Long, marketCapGrade: String): String {
        return ""
    }

    fun generateIpoSummary(stockName: String, sector: String): String {
        return "$stockName($sector) 신규 상장"
    }

    fun generateDelistingHeadline(stockName: String): String {
        return "[속보] $stockName, 상장폐지 결정"
    }

    fun generateDelistingContent(stockName: String, reason: String, finalPrice: Long): String {
        return ""
    }

    fun generateDelistingSummary(stockName: String): String {
        return "$stockName 상장폐지 결정"
    }
}
