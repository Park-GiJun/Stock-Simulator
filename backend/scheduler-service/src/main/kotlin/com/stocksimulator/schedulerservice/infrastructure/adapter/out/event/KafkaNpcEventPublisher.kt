package com.stocksimulator.schedulerservice.infrastructure.adapter.out.event

import com.stocksimulator.common.event.KafkaTopics
import com.stocksimulator.common.event.NpcCreatedEvent
import com.stocksimulator.schedulerservice.application.port.out.NpcEventPublishPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaNpcEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : NpcEventPublishPort {

    override fun publishNpcCreated(event: NpcCreatedEvent) {
        kafkaTemplate.send(KafkaTopics.NPC_CREATED, event.npcName, event)
    }
}
