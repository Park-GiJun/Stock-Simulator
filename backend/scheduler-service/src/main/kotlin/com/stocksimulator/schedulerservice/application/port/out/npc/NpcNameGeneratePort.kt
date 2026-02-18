package com.stocksimulator.schedulerservice.application.port.out.npc

interface NpcNameGeneratePort {
    suspend fun generate(): String
}
