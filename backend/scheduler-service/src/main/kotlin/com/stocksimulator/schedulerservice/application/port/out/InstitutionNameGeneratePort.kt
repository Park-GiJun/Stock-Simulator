package com.stocksimulator.schedulerservice.application.port.out

import com.stocksimulator.common.dto.InstitutionType

interface InstitutionNameGeneratePort {
    suspend fun generate(type: InstitutionType): String
}
