package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.repository

import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.entity.InstitutionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InstitutionJpaRepository : JpaRepository<InstitutionJpaEntity, Long> {

    fun findByInstitutionName(name: String): InstitutionJpaEntity?

    fun existsByInstitutionName(name: String): Boolean
}
