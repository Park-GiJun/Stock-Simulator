package com.stocksimulator.eventservice.application.port.out.stock

import com.stocksimulator.common.dto.Sector

interface CompanyNameGeneratePort {
    suspend fun generate(sector: Sector): String
    fun generateStockCode(): String
}
