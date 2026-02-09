package com.stocksimulator.schedulerservice.service

import com.stocksimulator.common.dto.Sector
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class CompanyNameGenerator {

    private val prefixes = listOf(
        "네오", "퓨처", "글로벌", "코리아", "한국", "대한", "유니", "하이", "스마트", "디지털",
        "그린", "블루", "골든", "실버", "프리미엄", "퍼스트", "뉴", "올", "탑", "베스트"
    )

    private val itKeywords = listOf(
        "테크", "소프트", "시스템", "데이터", "클라우드", "AI", "로봇", "반도체", "전자", "정보통신"
    )

    private val agricultureKeywords = listOf(
        "팜", "농산", "식품", "곡물", "축산", "원예", "종묘", "비료", "농기계", "유기농"
    )

    private val manufacturingKeywords = listOf(
        "산업", "제철", "기계", "자동차", "조선", "화학", "건설", "에너지", "플랜트", "소재"
    )

    private val serviceKeywords = listOf(
        "서비스", "유통", "물류", "운송", "관광", "호텔", "교육", "엔터", "미디어", "컨설팅"
    )

    private val realEstateKeywords = listOf(
        "건설", "개발", "부동산", "주택", "빌딩", "자산운용", "인프라", "도시개발", "리츠", "건축"
    )

    private val luxuryKeywords = listOf(
        "패션", "주얼리", "명품", "뷰티", "코스메틱", "아트", "갤러리", "럭셔리", "프리미엄", "하이엔드"
    )

    private val foodKeywords = listOf(
        "식품", "제과", "음료", "유제품", "가공식품", "냉동", "주류", "건강식품", "베이커리", "푸드"
    )

    private val suffixes = listOf(
        "", "홀딩스", "그룹", "컴퍼니", "코퍼레이션", "인더스트리", "파트너스", "캐피탈"
    )

    fun generate(sector: Sector): String {
        val prefix = prefixes.random()
        val keyword = when (sector) {
            Sector.IT -> itKeywords.random()
            Sector.AGRICULTURE -> agricultureKeywords.random()
            Sector.MANUFACTURING -> manufacturingKeywords.random()
            Sector.SERVICE -> serviceKeywords.random()
            Sector.REAL_ESTATE -> realEstateKeywords.random()
            Sector.LUXURY -> luxuryKeywords.random()
            Sector.FOOD -> foodKeywords.random()
        }
        val suffix = suffixes.random()

        return if (suffix.isEmpty()) {
            "$prefix$keyword"
        } else {
            "$prefix$keyword $suffix"
        }
    }

    fun generateStockCode(): String {
        val randomNumber = Random.nextInt(100000, 999999)
        return "A$randomNumber"
    }
}
