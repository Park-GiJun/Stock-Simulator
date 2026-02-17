package com.stocksimulator.stockservice.infrastructure.adapter.`in`.event

import com.stocksimulator.common.dto.InvestmentStyle
import com.stocksimulator.common.dto.Sector
import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.NpcCreatedEvent
import com.stocksimulator.stockservice.application.dto.command.npc.CreateNpcCommand
import com.stocksimulator.stockservice.application.port.`in`.npc.CreateNpcUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class NpcEventConsumer(
    private val createNpcUseCase: CreateNpcUseCase
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.NPC_CREATED], groupId = "stock-service")
    fun handleNpcCreated(event: NpcCreatedEvent, ack: Acknowledgment) {
        try {
            log.info("Received npc.created event: name={}, style={}",
                event.npcName, event.investmentStyle)

            val command = CreateNpcCommand(
                npcName = event.npcName,
                investmentStyle = InvestmentStyle.valueOf(event.investmentStyle),
                capital = event.capital,
                weeklyIncome = event.weeklyIncome,
                riskTolerance = event.riskTolerance,
                preferredSectors = event.preferredSectors.map { Sector.valueOf(it) },
                tradingFrequency = TradingFrequency.valueOf(event.tradingFrequency)
            )
            createNpcUseCase.createNpc(command)

            log.info("NPC created successfully: {}", event.npcName)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Failed to handle npc.created event: name={}", event.npcName, e)
            ack.acknowledge()
        }
    }
}
