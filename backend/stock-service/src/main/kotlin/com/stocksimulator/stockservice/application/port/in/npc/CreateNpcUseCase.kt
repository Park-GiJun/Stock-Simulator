package com.stocksimulator.stockservice.application.port.`in`.npc

import com.stocksimulator.stockservice.application.dto.command.npc.CreateNpcCommand

interface CreateNpcUseCase {
    fun createNpc(command: CreateNpcCommand)
}
