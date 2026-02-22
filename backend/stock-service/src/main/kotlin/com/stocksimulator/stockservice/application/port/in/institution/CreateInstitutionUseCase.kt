package com.stocksimulator.stockservice.application.port.`in`.institution

import com.stocksimulator.stockservice.application.dto.command.institution.CreateInstitutionCommand

interface CreateInstitutionUseCase {
    fun createInstitution(command: CreateInstitutionCommand)
}
