package com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.repository

import com.stocksimulator.common.dto.TradingFrequency
import com.stocksimulator.stockservice.infrastructure.adapter.out.persistence.institution.entity.InstitutionJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface InstitutionJpaRepository : JpaRepository<InstitutionJpaEntity, Long> {

    fun findByInstitutionName(name: String): InstitutionJpaEntity?

    fun existsByInstitutionName(name: String): Boolean

    fun findByTradingFrequency(tradingFrequency: TradingFrequency, pageable: Pageable): Page<InstitutionJpaEntity>
}
