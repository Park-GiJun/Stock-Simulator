package com.stocksimulator.stockservice.application.port.`in`.npc

import com.stocksimulator.stockservice.domain.model.NpcModel

interface GetNpcsByFrequencyUseCase {
    fun getNpcsByFrequency(frequency: String, maxCount: Int): List<NpcModel>
}
