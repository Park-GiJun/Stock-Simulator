package com.stocksimulator.schedulerservice.application.port.out.institution

import com.stocksimulator.common.dto.InstitutionType

interface InstitutionNameGeneratePort {
    suspend fun generate(type: InstitutionType): String
}
