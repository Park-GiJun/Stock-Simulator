package com.stocksimulator.stockservice.application.handler.institution

import com.stocksimulator.stockservice.application.dto.command.institution.CreateInstitutionCommand
import com.stocksimulator.stockservice.application.port.`in`.institution.CreateInstitutionUseCase
import com.stocksimulator.stockservice.application.port.`in`.institution.GetInstitutionListUseCase
import com.stocksimulator.stockservice.application.port.out.institution.InstitutionPersistencePort
import com.stocksimulator.stockservice.domain.model.InstitutionModel
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstitutionCommandHandler(
    private val institutionPersistencePort: InstitutionPersistencePort
) : CreateInstitutionUseCase, GetInstitutionListUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun createInstitution(command: CreateInstitutionCommand) {
        if (institutionPersistencePort.existsByName(command.institutionName)) {
            log.warn("Institution already exists, skipping: {}", command.institutionName)
            return
        }

        val newInstitution = InstitutionModel.create(
            institutionName = command.institutionName,
            institutionType = command.institutionType,
            investmentStyle = command.investmentStyle,
            capital = command.capital,
            riskTolerance = command.riskTolerance,
            preferredSectors = command.preferredSectors,
            tradingFrequency = command.tradingFrequency
        )

        val saved = institutionPersistencePort.save(newInstitution)
        log.info("Institution created: name={}, type={}, capital={}",
            saved.institutionName, saved.institutionType, saved.capital)
    }

    @Transactional(readOnly = true)
    override fun getInstitutions(page: Int, size: Int): Page<InstitutionModel> {
        return institutionPersistencePort.findAll(PageRequest.of(page, size))
    }
}
