package com.stocksimulator.stockservice.application.handler.npc

import com.stocksimulator.stockservice.application.dto.command.npc.CreateNpcCommand
import com.stocksimulator.stockservice.application.port.`in`.npc.CreateNpcUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetNpcListUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetNpcNamesUseCase
import com.stocksimulator.stockservice.application.port.out.npc.NpcPersistencePort
import com.stocksimulator.stockservice.domain.model.NpcModel
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NpcCommandHandler(
    private val npcPersistencePort: NpcPersistencePort
) : CreateNpcUseCase, GetNpcListUseCase, GetNpcNamesUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun createNpc(command: CreateNpcCommand) {
        val newNpc = NpcModel.create(
            npcName = command.npcName,
            investmentStyle = command.investmentStyle,
            capital = command.capital,
            riskTolerance = command.riskTolerance,
            preferredSectors = command.preferredSectors,
            tradingFrequency = command.tradingFrequency
        )

        val saved = npcPersistencePort.save(newNpc)
        log.info("NPC created: name={}, style={}, capital={}",
            saved.npcName, saved.investmentStyle, saved.capital)
    }

    @Transactional(readOnly = true)
    override fun getNpcs(page: Int, size: Int): Page<NpcModel> {
        return npcPersistencePort.findAll(PageRequest.of(page, size))
    }

    @Transactional(readOnly = true)
    override fun getNpcNames(): List<String> {
        return npcPersistencePort.findAllNames()
    }
}
