package com.stocksimulator.eventservice.application.port.out.institution

import com.stocksimulator.common.dto.InstitutionType

interface InstitutionNameGeneratePort {
    suspend fun generate(type: InstitutionType): String
}
