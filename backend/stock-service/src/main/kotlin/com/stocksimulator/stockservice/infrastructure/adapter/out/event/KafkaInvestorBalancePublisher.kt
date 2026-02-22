package com.stocksimulator.stockservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.InvestorBalanceInitEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.stockservice.application.port.out.InvestorBalanceEventPublishPort
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaInvestorBalancePublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : InvestorBalanceEventPublishPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishBalanceInit(investorId: String, investorType: String, initialCash: Long) {
        val event = InvestorBalanceInitEvent(
            investorId = investorId,
            investorType = investorType,
            initialCash = initialCash
        )
        kafkaTemplate.send(KafkaTopics.INVESTOR_BALANCE_INIT, investorId, event)
        log.info("잔고 초기화 이벤트 발행: investorId={}, type={}, cash={}", investorId, investorType, initialCash)
    }
}
