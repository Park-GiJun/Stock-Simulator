package com.stocksimulator.stockservice.application.port.`in`.institution

import com.stocksimulator.stockservice.domain.model.InstitutionModel

interface GetAllInstitutionsUseCase {
    fun getAllInstitutions(): List<InstitutionModel>
}
