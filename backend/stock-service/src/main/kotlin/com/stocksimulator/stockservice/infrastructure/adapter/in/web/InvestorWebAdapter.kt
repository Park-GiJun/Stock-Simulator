package com.stocksimulator.stockservice.infrastructure.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.PageResponse
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.stockservice.application.dto.result.institution.InstitutionResponse
import com.stocksimulator.stockservice.application.dto.result.npc.NpcResponse
import com.stocksimulator.stockservice.application.port.`in`.institution.GetInstitutionListUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetNpcListUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/investors")
@Tag(name = "Investor", description = "투자자 API")
class InvestorWebAdapter(
    private val getInstitutionListUseCase: GetInstitutionListUseCase,
    private val getNpcListUseCase: GetNpcListUseCase
) {

    @GetMapping("/institutions")
    @Operation(summary = "기관 투자자 목록 조회", description = "기관 투자자 목록을 페이지네이션으로 조회")
    fun getInstitutions(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<ApiResponse<PageResponse<InstitutionResponse>>>> = mono {
        val result = getInstitutionListUseCase.getInstitutions(page, size)
        val responseContent = result.content.map { InstitutionResponse.from(it) }
        val pageResponse = PageResponse.of(
            content = responseContent,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements
        )
        ApiResponse.success(pageResponse).toResponseEntity()
    }

    @GetMapping("/npcs")
    @Operation(summary = "개인 투자자(NPC) 목록 조회", description = "개인 투자자 목록을 페이지네이션으로 조회")
    fun getNpcs(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<ApiResponse<PageResponse<NpcResponse>>>> = mono {
        val result = getNpcListUseCase.getNpcs(page, size)
        val responseContent = result.content.map { NpcResponse.from(it) }
        val pageResponse = PageResponse.of(
            content = responseContent,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements
        )
        ApiResponse.success(pageResponse).toResponseEntity()
    }
}
