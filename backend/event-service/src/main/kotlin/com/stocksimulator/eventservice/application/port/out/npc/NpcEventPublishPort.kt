package com.stocksimulator.eventservice.application.port.out.npc

import com.stocksimulator.common.event.NpcCreatedEvent

interface NpcEventPublishPort {
    fun publishNpcCreated(event: NpcCreatedEvent)
}
