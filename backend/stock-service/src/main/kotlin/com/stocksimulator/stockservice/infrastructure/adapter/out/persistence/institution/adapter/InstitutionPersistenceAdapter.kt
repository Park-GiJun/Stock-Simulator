package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.adapter

import com.stocksimulator.stockservice.application.port.out.institution.InstitutionPersistencePort
import com.stocksimulator.stockservice.domain.model.InstitutionModel
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.entity.InstitutionJpaEntity
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.repository.InstitutionJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class InstitutionPersistenceAdapter(
    private val institutionJpaRepository: InstitutionJpaRepository
) : InstitutionPersistencePort {

    override fun save(model: InstitutionModel): InstitutionModel {
        return institutionJpaRepository.save(InstitutionJpaEntity.fromDomain(model)).toDomain()
    }

    override fun findByName(name: String): InstitutionModel? {
        return institutionJpaRepository.findByInstitutionName(name)?.toDomain()
    }

    override fun existsByName(name: String): Boolean {
        return institutionJpaRepository.existsByInstitutionName(name)
    }

    override fun findAll(pageable: Pageable): Page<InstitutionModel> {
        return institutionJpaRepository.findAll(pageable).map { it.toDomain() }
    }
}
