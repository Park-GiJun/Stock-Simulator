package com.stocksimulator.eventservice.application.port.`in`

import com.stocksimulator.common.event.EventOccurredEvent
import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent

interface PublishNewsUseCase {
    suspend fun publishIpoNews(event: StockListedEvent)
    suspend fun publishDelistingNews(event: StockDelistedEvent)
    suspend fun publishGameEventNews(event: EventOccurredEvent)
}
