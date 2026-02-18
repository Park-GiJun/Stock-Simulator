package com.stocksimulator.eventservice.application.port.out.institution

interface InstitutionExistenceCheckPort {
    fun existsByName(name: String): Boolean
}
