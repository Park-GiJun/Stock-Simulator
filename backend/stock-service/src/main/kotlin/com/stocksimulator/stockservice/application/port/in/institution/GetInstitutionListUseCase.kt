package com.stocksimulator.stockservice.application.port.`in`.institution

import com.stocksimulator.stockservice.domain.model.InstitutionModel
import org.springframework.data.domain.Page

interface GetInstitutionListUseCase {
    fun getInstitutions(page: Int, size: Int): Page<InstitutionModel>
}
