package com.stocksimulator.schedulerservice.application.port.out.institution

import com.stocksimulator.common.event.InstitutionCreatedEvent

interface InstitutionEventPublishPort {
    fun publishInstitutionCreated(event: InstitutionCreatedEvent)
}
