package com.stocksimulator.eventservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.InstitutionCreatedEvent
import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.eventservice.application.port.out.institution.InstitutionEventPublishPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaInstitutionEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : InstitutionEventPublishPort {

    override fun publishInstitutionCreated(event: InstitutionCreatedEvent) {
        kafkaTemplate.send(KafkaTopics.INSTITUTION_CREATED, event.institutionName, event)
    }
}
