package com.stocksimulator.eventservice.application.handler.trading

import com.stocksimulator.common.dto.OrderType
import com.stocksimulator.common.event.ScheduleTradeEvent
import com.stocksimulator.eventservice.application.port.out.NewsPersistencePort
import com.stocksimulator.eventservice.application.port.out.trading.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class InstitutionTradingHandler(
    private val investorProfileQueryPort: InvestorProfileQueryPort,
    private val investorPortfolioQueryPort: InvestorPortfolioQueryPort,
    private val stockCandidateQueryPort: StockCandidateQueryPort,
    private val tradingDecisionGeneratePort: TradingDecisionGeneratePort,
    private val tradeEventPublishPort: TradeEventPublishPort,
    private val newsPersistencePort: NewsPersistencePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun handleInstitutionTrading(tradingFrequency: String, maxBatchSize: Int) {
        log.info("기관투자자 자동매매 처리 시작: frequency={}, maxBatchSize={}", tradingFrequency, maxBatchSize)

        val institutions = investorProfileQueryPort.getInstitutionsByFrequency(tradingFrequency, maxBatchSize)
        if (institutions.isEmpty()) {
            log.info("해당 빈도의 기관투자자 없음: frequency={}", tradingFrequency)
            return
        }

        log.info("기관투자자 {}곳 매매 처리 시작: frequency={}", institutions.size, tradingFrequency)

        for (institution in institutions) {
            try {
                processInstitutionTrading(institution)
            } catch (e: Exception) {
                log.error("기관투자자 매매 처리 실패: institutionId={}, name={}, error={}",
                    institution.institutionId, institution.institutionName, e.message, e)
            }
        }

        log.info("기관투자자 자동매매 처리 완료: frequency={}, 처리수={}", tradingFrequency, institutions.size)
    }

    private suspend fun processInstitutionTrading(institution: InstitutionProfileDto) {
        val investorId = "INST_${institution.institutionId}"

        val balance = investorPortfolioQueryPort.getBalance(investorId, "INSTITUTION")
        val holdings = investorPortfolioQueryPort.getPortfolioHoldings(investorId, "INSTITUTION")

        val stockCandidates = institution.preferredSectors.flatMap { sector ->
            stockCandidateQueryPort.getStocksBySector(sector, 5)
        }.distinctBy { it.stockId }.take(10)

        val recentNews = try {
            val newsPage = newsPersistencePort.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"))
            )
            newsPage.content.map { news ->
                NewsInfoDto(
                    headline = news.headline,
                    sentiment = news.sentiment.name,
                    sector = news.sector
                )
            }
        } catch (e: Exception) {
            log.warn("뉴스 조회 실패, 빈 목록으로 진행: {}", e.message)
            emptyList()
        }

        val availableCash = if (balance > 0) balance else institution.capital

        val request = TradingDecisionRequest(
            investorName = institution.institutionName,
            investmentStyle = institution.investmentStyle,
            riskTolerance = institution.riskTolerance,
            availableCash = availableCash,
            preferredSectors = institution.preferredSectors,
            currentHoldings = holdings,
            stockCandidates = stockCandidates,
            recentNews = recentNews
        )

        val decision = tradingDecisionGeneratePort.generateDecision(request)

        log.info("기관투자자 매매 결정: institution={}, action={}, stockId={}, quantity={}, reason={}",
            institution.institutionName, decision.action, decision.stockId, decision.quantity, decision.reason)

        if (decision.action == "HOLD" || decision.stockId == null) {
            return
        }

        val orderType = when (decision.action) {
            "BUY" -> OrderType.BUY
            "SELL" -> OrderType.SELL
            else -> return
        }

        val event = ScheduleTradeEvent(
            investorId = investorId,
            investorType = "INSTITUTION",
            stockId = decision.stockId,
            orderType = orderType,
            quantity = decision.quantity,
            reason = decision.reason
        )

        tradeEventPublishPort.publishScheduleTrade(event)
    }
}
