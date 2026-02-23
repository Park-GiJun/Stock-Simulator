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
class NpcTradingHandler(
    private val investorProfileQueryPort: InvestorProfileQueryPort,
    private val investorPortfolioQueryPort: InvestorPortfolioQueryPort,
    private val stockCandidateQueryPort: StockCandidateQueryPort,
    private val tradingDecisionGeneratePort: TradingDecisionGeneratePort,
    private val tradeEventPublishPort: TradeEventPublishPort,
    private val newsPersistencePort: NewsPersistencePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun handleNpcTrading() {
        log.info("NPC 자동매매 처리 시작 (전체 대상)")

        val npcs = investorProfileQueryPort.getAllNpcs()
        if (npcs.isEmpty()) {
            log.info("등록된 NPC 없음")
            return
        }

        // 전체 종목 한 번만 조회
        val allStocks = stockCandidateQueryPort.getAllStocks(100)
        if (allStocks.isEmpty()) {
            log.info("상장 종목 없음, 매매 스킵")
            return
        }

        val recentNews = fetchRecentNews()

        var executedCount = 0

        for (npc in npcs) {
            try {
                processNpcTrading(npc, allStocks, recentNews)
                executedCount++
            } catch (e: Exception) {
                log.error("NPC 매매 처리 실패: npcId={}, name={}, error={}",
                    npc.npcId, npc.npcName, e.message, e)
            }
        }

        log.info("NPC 자동매매 처리 완료: 전체={}, 실행={}", npcs.size, executedCount)
    }

    private fun fetchRecentNews(): List<NewsInfoDto> {
        return try {
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
    }

    private suspend fun processNpcTrading(npc: NpcProfileDto, allStocks: List<StockCandidateDto>, recentNews: List<NewsInfoDto>) {
        val investorId = "NPC_${npc.npcId}"

        val balance = investorPortfolioQueryPort.getBalance(investorId, "NPC")
        val holdings = investorPortfolioQueryPort.getPortfolioHoldings(investorId, "NPC")

        val availableCash = if (balance > 0) balance else npc.capital

        val allowedActions = mutableListOf<String>()
        if (availableCash > 0 && allStocks.isNotEmpty()) allowedActions.add("BUY")
        if (holdings.isNotEmpty()) allowedActions.add("SELL")

        if (allowedActions.isEmpty()) {
            return
        }

        val request = TradingDecisionRequest(
            investorName = npc.npcName,
            investmentStyle = npc.investmentStyle,
            riskTolerance = npc.riskTolerance,
            availableCash = availableCash,
            preferredSectors = npc.preferredSectors,
            currentHoldings = holdings,
            stockCandidates = allStocks,
            recentNews = recentNews,
            allowedActions = allowedActions
        )

        val decision = tradingDecisionGeneratePort.generateDecision(request)

        log.info("NPC 매매 결정: npc={}, action={}, stockId={}, quantity={}",
            npc.npcName, decision.action, decision.stockId, decision.quantity)

        if (decision.action == "HOLD" || decision.stockId == null || decision.action !in allowedActions) {
            return
        }

        val orderType = when (decision.action) {
            "BUY" -> OrderType.BUY
            "SELL" -> OrderType.SELL
            else -> return
        }

        val event = ScheduleTradeEvent(
            investorId = investorId,
            investorType = "NPC",
            stockId = decision.stockId,
            orderType = orderType,
            quantity = decision.quantity
        )

        tradeEventPublishPort.publishScheduleTrade(event)
    }
}
