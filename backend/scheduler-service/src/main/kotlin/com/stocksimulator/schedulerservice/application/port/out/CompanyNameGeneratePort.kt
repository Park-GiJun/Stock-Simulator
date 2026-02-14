package com.stocksimulator.schedulerservice.application.port.out

import com.stocksimulator.common.dto.Sector

interface CompanyNameGeneratePort {
    suspend fun generate(sector: Sector): String
    fun generateStockCode(): String
}
