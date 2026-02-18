package com.stocksimulator.eventservice.application.port.out.institution

import com.stocksimulator.common.event.InstitutionCreatedEvent

interface InstitutionEventPublishPort {
    fun publishInstitutionCreated(event: InstitutionCreatedEvent)
}
