package com.stocksimulator.stockservice.infrastructure.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.PageResponse
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.stockservice.application.dto.result.institution.InstitutionResult
import com.stocksimulator.stockservice.application.dto.result.npc.NpcResult
import com.stocksimulator.stockservice.application.port.`in`.institution.CheckInstitutionExistsUseCase
import com.stocksimulator.stockservice.application.port.`in`.institution.GetAllInstitutionsUseCase
import com.stocksimulator.stockservice.application.port.`in`.institution.GetInstitutionListUseCase
import com.stocksimulator.stockservice.application.port.`in`.institution.GetInstitutionsByFrequencyUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetAllNpcsUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetNpcListUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetNpcNamesUseCase
import com.stocksimulator.stockservice.application.port.`in`.npc.GetNpcsByFrequencyUseCase
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
    private val getNpcListUseCase: GetNpcListUseCase,
    private val getNpcNamesUseCase: GetNpcNamesUseCase,
    private val checkInstitutionExistsUseCase: CheckInstitutionExistsUseCase,
    private val getNpcsByFrequencyUseCase: GetNpcsByFrequencyUseCase,
    private val getInstitutionsByFrequencyUseCase: GetInstitutionsByFrequencyUseCase,
    private val getAllNpcsUseCase: GetAllNpcsUseCase,
    private val getAllInstitutionsUseCase: GetAllInstitutionsUseCase
) {

    @GetMapping("/institutions")
    @Operation(summary = "기관 투자자 목록 조회", description = "기관 투자자 목록을 페이지네이션으로 조회")
    fun getInstitutions(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<ApiResponse<PageResponse<InstitutionResult>>>> = mono {
        val result = getInstitutionListUseCase.getInstitutions(page, size)
        val responseContent = result.content.map { InstitutionResult.from(it) }
        val pageResponse = PageResponse.of(
            content = responseContent,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements
        )
        ApiResponse.success(pageResponse).toResponseEntity()
    }

    @GetMapping("/institutions/exists")
    @Operation(summary = "기관 투자자 존재 여부 확인", description = "기관명으로 존재 여부 확인")
    fun checkInstitutionExists(
        @Parameter(description = "기관명") @RequestParam name: String
    ): Mono<ResponseEntity<ApiResponse<Boolean>>> = mono {
        val exists = checkInstitutionExistsUseCase.existsByInstitutionName(name)
        ApiResponse.success(exists).toResponseEntity()
    }

    @GetMapping("/institutions/by-frequency")
    @Operation(summary = "빈도별 기관투자자 조회", description = "거래 빈도별 기관투자자 목록 조회")
    fun getInstitutionsByFrequency(
        @Parameter(description = "거래 빈도 (HIGH, MEDIUM, LOW)") @RequestParam frequency: String,
        @Parameter(description = "최대 조회 수") @RequestParam(defaultValue = "3") maxCount: Int
    ): Mono<ResponseEntity<ApiResponse<List<InstitutionResult>>>> = mono {
        val institutions = getInstitutionsByFrequencyUseCase.getInstitutionsByFrequency(frequency, maxCount)
        val results = institutions.map { InstitutionResult.from(it) }
        ApiResponse.success(results).toResponseEntity()
    }

    @GetMapping("/npcs")
    @Operation(summary = "개인 투자자(NPC) 목록 조회", description = "개인 투자자 목록을 페이지네이션으로 조회")
    fun getNpcs(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<ApiResponse<PageResponse<NpcResult>>>> = mono {
        val result = getNpcListUseCase.getNpcs(page, size)
        val responseContent = result.content.map { NpcResult.from(it) }
        val pageResponse = PageResponse.of(
            content = responseContent,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements
        )
        ApiResponse.success(pageResponse).toResponseEntity()
    }

    @GetMapping("/npcs/names")
    @Operation(summary = "NPC 이름 목록 조회", description = "모든 NPC 이름 목록을 조회 (중복 확인용)")
    fun getNpcNames(): Mono<ResponseEntity<ApiResponse<List<String>>>> = mono {
        val names = getNpcNamesUseCase.getNpcNames()
        ApiResponse.success(names).toResponseEntity()
    }

    @GetMapping("/npcs/by-frequency")
    @Operation(summary = "빈도별 NPC 조회", description = "거래 빈도별 NPC 목록 조회")
    fun getNpcsByFrequency(
        @Parameter(description = "거래 빈도 (HIGH, MEDIUM, LOW)") @RequestParam frequency: String,
        @Parameter(description = "최대 조회 수") @RequestParam(defaultValue = "5") maxCount: Int
    ): Mono<ResponseEntity<ApiResponse<List<NpcResult>>>> = mono {
        val npcs = getNpcsByFrequencyUseCase.getNpcsByFrequency(frequency, maxCount)
        val results = npcs.map { NpcResult.from(it) }
        ApiResponse.success(results).toResponseEntity()
    }

    @GetMapping("/npcs/all")
    @Operation(summary = "전체 NPC 조회", description = "모든 NPC 목록을 조회 (자동매매용)")
    fun getAllNpcs(): Mono<ResponseEntity<ApiResponse<List<NpcResult>>>> = mono {
        val npcs = getAllNpcsUseCase.getAllNpcs()
        val results = npcs.map { NpcResult.from(it) }
        ApiResponse.success(results).toResponseEntity()
    }

    @GetMapping("/institutions/all")
    @Operation(summary = "전체 기관투자자 조회", description = "모든 기관투자자 목록을 조회 (자동매매용)")
    fun getAllInstitutions(): Mono<ResponseEntity<ApiResponse<List<InstitutionResult>>>> = mono {
        val institutions = getAllInstitutionsUseCase.getAllInstitutions()
        val results = institutions.map { InstitutionResult.from(it) }
        ApiResponse.success(results).toResponseEntity()
    }
}
