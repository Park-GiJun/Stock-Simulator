package com.stocksimulator.stockservice.domain.model

import com.stocksimulator.common.dto.InstitutionType

data class Institution(
    val institutionId: Long?,
    val institutionName: String,
    val institutionType: InstitutionType

)
