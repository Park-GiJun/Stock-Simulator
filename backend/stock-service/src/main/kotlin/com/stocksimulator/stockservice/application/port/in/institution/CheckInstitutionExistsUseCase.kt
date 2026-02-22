package com.stocksimulator.stockservice.application.port.`in`.institution

interface CheckInstitutionExistsUseCase {
    fun existsByInstitutionName(name: String): Boolean
}
