package com.stocksimulator.schedulerservice.application.port.out

import com.stocksimulator.common.event.InstitutionCreatedEvent

interface InstitutionEventPublishPort {
    fun publishInstitutionCreated(event: InstitutionCreatedEvent)
}
