package com.stocksimulator.schedulerservice.application.port.out

interface TriggerPublishPort {
    fun publishStockListingTrigger()
    fun publishStockDelistingTrigger()
    fun publishNpcCreationTrigger(count: Int)
    fun publishInstitutionCreationTrigger()
    fun publishNewsGenerationTrigger(level: String)
    fun publishNpcTradingTrigger()
    fun publishInstitutionTradingTrigger()
}
