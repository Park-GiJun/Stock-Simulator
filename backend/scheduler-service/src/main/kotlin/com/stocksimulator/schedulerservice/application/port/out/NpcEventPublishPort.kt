package com.stocksimulator.schedulerservice.application.port.out

import com.stocksimulator.common.event.NpcCreatedEvent

interface NpcEventPublishPort {
    fun publishNpcCreated(event: NpcCreatedEvent)
}
