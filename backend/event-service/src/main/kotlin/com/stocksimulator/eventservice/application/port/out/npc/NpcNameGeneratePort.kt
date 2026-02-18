package com.stocksimulator.eventservice.application.port.out.npc

interface NpcNameGeneratePort {
    suspend fun generate(existingNames: List<String> = emptyList()): String
}
