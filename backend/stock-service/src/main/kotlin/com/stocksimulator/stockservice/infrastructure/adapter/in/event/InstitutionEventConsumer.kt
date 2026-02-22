package com.stocksimulator.stockservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.InstitutionType
import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.common.event.InstitutionCreatedEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.stockservice.application.dto.command.institution.CreateInstitutionCommand
import com.stocksimulator.stockservice.application.port.`in`.institution.CreateInstitutionUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class InstitutionEventConsumer(
    private val createInstitutionUseCase: CreateInstitutionUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.INSTITUTION_CREATED], groupId = "stock-service")
    fun handleInstitutionCreated(event: InstitutionCreatedEvent, ack: Acknowledgment) {
        try {
            log.info("Received institution.created event: name={}, type={}",
                event.institutionName, event.institutionType)

            val command = CreateInstitutionCommand(
                institutionName = event.institutionName,
                institutionType = InstitutionType.valueOf(event.institutionType),
                investmentStyle = InvestmentStyle.valueOf(event.investmentStyle),
                capital = event.capital,
                dailyIncome = event.dailyIncome,
                riskTolerance = event.riskTolerance,
                preferredSectors = event.preferredSectors.map { Sector.valueOf(it) },
                tradingFrequency = TradingFrequency.valueOf(event.tradingFrequency)
            )
            createInstitutionUseCase.createInstitution(command)

            log.info("Institution created successfully: {}", event.institutionName)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle institution.created event: name={}", event.institutionName, e)
            ack.acknowledge()
        }
    }
}
