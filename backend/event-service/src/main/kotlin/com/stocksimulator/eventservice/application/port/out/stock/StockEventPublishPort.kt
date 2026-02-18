package com.stocksimulator.eventservice.application.port.out.stock

import com.stocksimulator.common.event.StockDelistedEvent
import com.stocksimulator.common.event.StockListedEvent

interface StockEventPublishPort {
    fun publishStockListed(event: StockListedEvent)
    fun publishStockDelisted(event: StockDelistedEvent)
}
