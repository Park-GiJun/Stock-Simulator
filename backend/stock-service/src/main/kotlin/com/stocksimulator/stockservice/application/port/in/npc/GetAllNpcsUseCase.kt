package com.stocksimulator.stockservice.application.port.`in`.npc

import com.stocksimulator.stockservice.domain.model.NpcModel

interface GetAllNpcsUseCase {
    fun getAllNpcs(): List<NpcModel>
}
