package com.stocksimulator.schedulerservice.application.port.out

interface NpcNameGeneratePort {
    suspend fun generate(): String
}
