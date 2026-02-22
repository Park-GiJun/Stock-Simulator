package com.stocksimulator.eventservice.application.port.out.trading

import com.stocksimulator.common.event.ScheduleTradeEvent

interface TradeEventPublishPort {
    fun publishScheduleTrade(event: ScheduleTradeEvent)
}
