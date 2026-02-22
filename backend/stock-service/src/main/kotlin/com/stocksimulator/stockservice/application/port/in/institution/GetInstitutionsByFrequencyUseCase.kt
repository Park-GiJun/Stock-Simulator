package com.stocksimulator.stockservice.application.port.`in`.institution

import com.stocksimulator.stockservice.domain.model.InstitutionModel

interface GetInstitutionsByFrequencyUseCase {
    fun getInstitutionsByFrequency(frequency: String, maxCount: Int): List<InstitutionModel>
}
