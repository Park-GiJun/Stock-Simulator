package com.stocksimulator.eventservice.infrastructure.adapter.out.generator

import com.stocksimulator.common.dto.EventLevel
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.Sentiment
import com.stocksimulator.eventservice.application.port.out.news.GeneratedNewsContent
import com.stocksimulator.eventservice.application.port.out.news.NewsContentGeneratePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class NewsContentGeneratorWithLLM : NewsContentGeneratePort {

    private val log = LoggerFactory.getLogger(javaClass)

    data class NewsTemplate(
        val headline: String,
        val sentiment: Sentiment,
        val intensityRange: ClosedFloatingPointRange<Double>,
        val durationRange: LongRange
    )

    // 기업 뉴스 템플릿 (섹터별)
    private val companyNewsBySector: Map<Sector, List<NewsTemplate>> = mapOf(
        Sector.IT to listOf(
            NewsTemplate("{stock} 신규 AI 서비스 출시 예정", Sentiment.POSITIVE, 0.3..0.7, 60L..240L),
            NewsTemplate("{stock} 데이터센터 대규모 투자 발표", Sentiment.POSITIVE, 0.5..0.8, 120L..360L),
            NewsTemplate("{stock} 반도체 수요 급증으로 실적 호조", Sentiment.POSITIVE, 0.4..0.7, 90L..300L),
            NewsTemplate("{stock} 보안 사고 발생, 고객 데이터 유출 우려", Sentiment.NEGATIVE, 0.5..0.9, 120L..480L),
            NewsTemplate("{stock} 클라우드 사업부 구조조정 착수", Sentiment.NEGATIVE, 0.3..0.6, 60L..240L),
            NewsTemplate("{stock} 핵심 인력 대거 이탈 소식", Sentiment.NEGATIVE, 0.4..0.7, 90L..300L),
            NewsTemplate("{stock} 차세대 플랫폼 개발 순항", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 분기 실적 시장 기대치 부합", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L),
            NewsTemplate("{stock} 해외 시장 진출 본격화", Sentiment.POSITIVE, 0.4..0.6, 90L..240L),
            NewsTemplate("{stock} 경쟁사 대비 점유율 하락 추세", Sentiment.NEGATIVE, 0.3..0.5, 60L..180L)
        ),
        Sector.AGRICULTURE to listOf(
            NewsTemplate("{stock} 스마트팜 기술 도입 성과 가시화", Sentiment.POSITIVE, 0.3..0.6, 60L..240L),
            NewsTemplate("{stock} 올해 작황 역대급 풍년 전망", Sentiment.POSITIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("{stock} 수출 물량 전년 대비 30% 증가", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("{stock} 가뭄 피해로 원재료 수급 차질", Sentiment.NEGATIVE, 0.5..0.8, 120L..360L),
            NewsTemplate("{stock} 수입 농산물 규제 완화에 타격", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("{stock} 병충해 확산으로 생산량 감소", Sentiment.NEGATIVE, 0.4..0.7, 120L..300L),
            NewsTemplate("{stock} 친환경 인증 획득으로 브랜드 제고", Sentiment.POSITIVE, 0.2..0.5, 60L..180L),
            NewsTemplate("{stock} 분기 매출 소폭 증가", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
        ),
        Sector.MANUFACTURING to listOf(
            NewsTemplate("{stock} 신공장 준공으로 생산능력 2배 확대", Sentiment.POSITIVE, 0.5..0.8, 120L..360L),
            NewsTemplate("{stock} 대규모 수주 계약 체결 성공", Sentiment.POSITIVE, 0.4..0.7, 90L..300L),
            NewsTemplate("{stock} 자동화 라인 도입으로 원가 절감", Sentiment.POSITIVE, 0.3..0.6, 60L..240L),
            NewsTemplate("{stock} 원자재 가격 급등으로 마진 압박", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("{stock} 공장 가동률 하락, 재고 누적", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("{stock} 품질 문제로 대규모 리콜 실시", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
            NewsTemplate("{stock} 해외 합작법인 설립 추진", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 노사 협상 원만히 타결", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
        ),
        Sector.SERVICE to listOf(
            NewsTemplate("{stock} 신규 서비스 가입자 100만 돌파", Sentiment.POSITIVE, 0.4..0.7, 90L..300L),
            NewsTemplate("{stock} 프랜차이즈 확장 가속화", Sentiment.POSITIVE, 0.3..0.6, 60L..240L),
            NewsTemplate("{stock} 고객 만족도 업계 1위 달성", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 인건비 상승으로 수익성 악화", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("{stock} 핵심 고객사 계약 해지 통보", Sentiment.NEGATIVE, 0.5..0.8, 120L..360L),
            NewsTemplate("{stock} 서비스 장애 발생, 고객 불만 증가", Sentiment.NEGATIVE, 0.4..0.7, 60L..240L),
            NewsTemplate("{stock} 디지털 전환 투자 확대 계획", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 분기 실적 전년 수준 유지", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
        ),
        Sector.REAL_ESTATE to listOf(
            NewsTemplate("{stock} 대규모 개발사업 인허가 획득", Sentiment.POSITIVE, 0.5..0.8, 120L..360L),
            NewsTemplate("{stock} 분양 완판 기록, 추가 사업 추진", Sentiment.POSITIVE, 0.4..0.7, 90L..300L),
            NewsTemplate("{stock} 임대수익 안정적 성장세", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 금리 인상에 따른 PF 부담 가중", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
            NewsTemplate("{stock} 미분양 물량 급증 우려", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("{stock} 건설비 상승으로 사업성 저하", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("{stock} 신규 랜드마크 프로젝트 발표", Sentiment.POSITIVE, 0.4..0.6, 90L..240L),
            NewsTemplate("{stock} 부동산 시장 관망세 지속", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
        ),
        Sector.LUXURY to listOf(
            NewsTemplate("{stock} 한정판 컬렉션 출시 즉시 완판", Sentiment.POSITIVE, 0.4..0.7, 60L..240L),
            NewsTemplate("{stock} 글로벌 셀럽 브랜드 앰배서더 계약", Sentiment.POSITIVE, 0.3..0.6, 60L..180L),
            NewsTemplate("{stock} 아시아 시장 매출 사상 최대", Sentiment.POSITIVE, 0.5..0.7, 90L..300L),
            NewsTemplate("{stock} 소비 심리 위축으로 매출 감소", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("{stock} 위조품 대량 유통 적발, 브랜드 타격", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("{stock} 고가 전략 역풍, 소비자 이탈", Sentiment.NEGATIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 온라인 채널 매출 비중 확대", Sentiment.POSITIVE, 0.2..0.5, 60L..180L),
            NewsTemplate("{stock} 브랜드 리뉴얼 진행 중", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
        ),
        Sector.FOOD to listOf(
            NewsTemplate("{stock} 신제품 출시 초기 반응 폭발적", Sentiment.POSITIVE, 0.4..0.7, 60L..240L),
            NewsTemplate("{stock} 해외 수출 계약 대규모 체결", Sentiment.POSITIVE, 0.4..0.7, 90L..300L),
            NewsTemplate("{stock} 건강식품 트렌드 수혜 기대", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("{stock} 식품 안전 이슈 발생, 자진 회수", Sentiment.NEGATIVE, 0.5..0.9, 120L..480L),
            NewsTemplate("{stock} 원재료 가격 상승으로 제품가 인상", Sentiment.NEGATIVE, 0.3..0.5, 60L..240L),
            NewsTemplate("{stock} 소비자 불매운동 확산 조짐", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("{stock} 유기농 라인업 확대 계획", Sentiment.POSITIVE, 0.2..0.5, 60L..180L),
            NewsTemplate("{stock} 분기 매출 전년 대비 보합", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
        )
    )

    // 산업 뉴스 템플릿 (섹터별)
    private val industryNewsBySector: Map<Sector, List<NewsTemplate>> = mapOf(
        Sector.IT to listOf(
            NewsTemplate("IT업계 AI 투자 열풍, 관련주 동반 상승", Sentiment.POSITIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("반도체 업황 회복세, IT주 강세 전망", Sentiment.POSITIVE, 0.4..0.7, 120L..300L),
            NewsTemplate("IT 수출 호조, 업계 전반 수혜 기대", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("글로벌 IT 기업 대규모 감원 물결", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("사이버 보안 위협 증가, IT업계 긴장", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("IT 규제 강화 법안 국회 통과", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L)
        ),
        Sector.AGRICULTURE to listOf(
            NewsTemplate("정부, 농업 보조금 대폭 확대 발표", Sentiment.POSITIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("스마트농업 육성 정책 본격 시행", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("농산물 수출 사상 최대 실적 전망", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("이상기후로 전국 농작물 피해 속출", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
            NewsTemplate("농산물 수입 개방 확대, 국내 농가 타격", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("비료 가격 폭등, 농업계 원가 부담 가중", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L)
        ),
        Sector.MANUFACTURING to listOf(
            NewsTemplate("제조업 수출 7개월 연속 증가세", Sentiment.POSITIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("스마트팩토리 보급 확대, 제조업 혁신 가속", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("제조업 경기실사지수 상승 반전", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("글로벌 공급망 차질, 제조업체 타격", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
            NewsTemplate("제조업 전력비 인상안 확정, 원가 부담", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("제조업 인력난 심화, 생산 차질 우려", Sentiment.NEGATIVE, 0.3..0.5, 60L..180L)
        ),
        Sector.SERVICE to listOf(
            NewsTemplate("서비스업 경기 회복세 뚜렷", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("비대면 서비스 시장 급성장 지속", Sentiment.POSITIVE, 0.4..0.7, 120L..300L),
            NewsTemplate("관광·레저 산업 V자 반등 기대", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("최저임금 인상, 서비스업계 부담 가중", Sentiment.NEGATIVE, 0.3..0.6, 120L..360L),
            NewsTemplate("서비스업 폐업률 증가 추세", Sentiment.NEGATIVE, 0.3..0.5, 90L..240L),
            NewsTemplate("소비 위축에 서비스업 실적 악화 전망", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L)
        ),
        Sector.REAL_ESTATE to listOf(
            NewsTemplate("정부 부동산 규제 완화 정책 발표", Sentiment.POSITIVE, 0.5..0.8, 120L..480L),
            NewsTemplate("주택시장 거래량 반등, 회복 신호", Sentiment.POSITIVE, 0.4..0.6, 90L..240L),
            NewsTemplate("재개발·재건축 기대감 확산", Sentiment.POSITIVE, 0.3..0.6, 90L..300L),
            NewsTemplate("부동산 PF 부실 우려 확산", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
            NewsTemplate("주택 가격 하락세 지속, 시장 위축", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("금리 인상에 부동산업계 한파", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L)
        ),
        Sector.LUXURY to listOf(
            NewsTemplate("럭셔리 시장 성장세 지속, 매출 호조", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("MZ세대 명품 소비 트렌드 확산", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
            NewsTemplate("럭셔리 브랜드 한국 시장 투자 확대", Sentiment.POSITIVE, 0.4..0.6, 90L..240L),
            NewsTemplate("경기 침체에 럭셔리 소비 급감", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("명품 관세 인상안 추진, 업계 반발", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("럭셔리 브랜드 가격 인상 러시, 소비자 피로감", Sentiment.NEGATIVE, 0.3..0.5, 60L..180L)
        ),
        Sector.FOOD to listOf(
            NewsTemplate("식품업계 건강식품 시장 급성장", Sentiment.POSITIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("K-푸드 글로벌 인기 급상승", Sentiment.POSITIVE, 0.4..0.7, 120L..300L),
            NewsTemplate("식품 수출 사상 최대 실적 기록", Sentiment.POSITIVE, 0.4..0.6, 90L..240L),
            NewsTemplate("식품 원재료 가격 동반 급등", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
            NewsTemplate("식품 안전 규제 대폭 강화 예고", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
            NewsTemplate("식품업계 과당 경쟁 심화", Sentiment.NEGATIVE, 0.3..0.5, 60L..180L)
        )
    )

    // 사회 뉴스 템플릿
    private val societyNews: List<NewsTemplate> = listOf(
        NewsTemplate("한국은행, 기준금리 0.25%p 인하 결정", Sentiment.POSITIVE, 0.5..0.8, 240L..480L),
        NewsTemplate("정부, 대규모 경기부양책 발표", Sentiment.POSITIVE, 0.6..0.9, 240L..480L),
        NewsTemplate("수출 실적 사상 최대 기록 경신", Sentiment.POSITIVE, 0.4..0.7, 120L..360L),
        NewsTemplate("GDP 성장률 전망치 상향 조정", Sentiment.POSITIVE, 0.4..0.7, 120L..360L),
        NewsTemplate("외국인 투자자 대규모 순매수 전환", Sentiment.POSITIVE, 0.4..0.6, 90L..240L),
        NewsTemplate("소비자 심리지수 큰 폭 상승", Sentiment.POSITIVE, 0.3..0.5, 60L..180L),
        NewsTemplate("정부, 세제 혜택 확대 정책 추진", Sentiment.POSITIVE, 0.3..0.6, 90L..300L),
        NewsTemplate("글로벌 교역 확대, 한국 경제 수혜 전망", Sentiment.POSITIVE, 0.3..0.6, 120L..300L),
        NewsTemplate("한국은행, 기준금리 0.25%p 인상 결정", Sentiment.NEGATIVE, 0.5..0.8, 240L..480L),
        NewsTemplate("미중 무역갈등 재점화, 글로벌 시장 불안", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
        NewsTemplate("국제유가 급등, 인플레이션 우려 확산", Sentiment.NEGATIVE, 0.4..0.7, 120L..360L),
        NewsTemplate("글로벌 경기침체 경고음 확대", Sentiment.NEGATIVE, 0.5..0.8, 120L..480L),
        NewsTemplate("외국인 투자자 대규모 순매도 지속", Sentiment.NEGATIVE, 0.4..0.7, 90L..300L),
        NewsTemplate("가계부채 사상 최대, 소비 위축 우려", Sentiment.NEGATIVE, 0.3..0.6, 120L..360L),
        NewsTemplate("지정학적 리스크 고조, 시장 변동성 확대", Sentiment.NEGATIVE, 0.4..0.7, 90L..300L),
        NewsTemplate("환율 급변동, 수출입 기업 불확실성 증가", Sentiment.NEGATIVE, 0.3..0.6, 90L..240L),
        NewsTemplate("주요 경제지표 시장 예상 부합", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L),
        NewsTemplate("코스피 박스권 등락, 관망세 지속", Sentiment.NEUTRAL, 0.1..0.2, 30L..90L),
        NewsTemplate("주요 기관 경제 전망 엇갈려", Sentiment.NEUTRAL, 0.1..0.3, 30L..120L)
    )

    override suspend fun generateNews(level: EventLevel, sector: Sector?, stockName: String?): GeneratedNewsContent {
        val template = selectTemplate(level, sector)

        val headline = if (stockName != null) {
            template.headline.replace("{stock}", stockName)
        } else {
            template.headline.replace("{stock}", "")
        }

        val intensity = Random.nextDouble(template.intensityRange.start, template.intensityRange.endInclusive)
        val duration = Random.nextLong(template.durationRange.first, template.durationRange.last + 1)

        log.info("뉴스 생성 완료: level={}, sector={}, sentiment={}, headline={}", level, sector?.name, template.sentiment, headline)

        return GeneratedNewsContent(
            headline = headline.trim(),
            content = "",
            sentiment = template.sentiment,
            intensity = intensity.coerceIn(0.1, 1.0),
            duration = duration.coerceIn(30, 480)
        )
    }

    private fun selectTemplate(level: EventLevel, sector: Sector?): NewsTemplate {
        return when (level) {
            EventLevel.COMPANY -> {
                val sectorTemplates = if (sector != null) {
                    companyNewsBySector[sector] ?: companyNewsBySector.values.flatten()
                } else {
                    companyNewsBySector.values.flatten()
                }
                sectorTemplates.random()
            }
            EventLevel.INDUSTRY -> {
                val sectorTemplates = if (sector != null) {
                    industryNewsBySector[sector] ?: industryNewsBySector.values.flatten()
                } else {
                    industryNewsBySector.values.flatten()
                }
                sectorTemplates.random()
            }
            EventLevel.SOCIETY -> societyNews.random()
        }
    }
}
