package com.stocksimulator.stockservice.application.port.out.institution

import com.stocksimulator.stockservice.domain.model.InstitutionModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface InstitutionPersistencePort {
    fun save(model: InstitutionModel): InstitutionModel
    fun findByName(name: String): InstitutionModel?
    fun existsByName(name: String): Boolean
    fun findAll(pageable: Pageable): Page<InstitutionModel>
}
