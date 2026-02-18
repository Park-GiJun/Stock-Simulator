package com.stocksimulator.eventservice.application.port.out.npc

interface NpcNameQueryPort {
    fun getAllNpcNames(): List<String>
}
