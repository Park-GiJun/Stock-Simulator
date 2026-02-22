package com.stocksimulator.stockservice.application.port.`in`.npc

import com.stocksimulator.stockservice.domain.model.NpcModel
import org.springframework.data.domain.Page

interface GetNpcListUseCase {
    fun getNpcs(page: Int, size: Int): Page<NpcModel>
}
