package com.stocksimulator.userservice.adapter.`in`.web

import com.stocksimulator.common.dto.ApiResponse
import com.stocksimulator.common.dto.toCreatedResponseEntity
import com.stocksimulator.common.dto.toResponseEntity
import com.stocksimulator.userservice.adapter.`in`.web.dto.LoginRequest
import com.stocksimulator.userservice.adapter.`in`.web.dto.LoginResponse
import com.stocksimulator.userservice.adapter.`in`.web.dto.SignUpRequest
import com.stocksimulator.userservice.adapter.`in`.web.dto.SignUpResponse
import com.stocksimulator.userservice.adapter.`in`.web.dto.UserResponse
import com.stocksimulator.userservice.application.dto.command.user.LoginCommand
import com.stocksimulator.userservice.application.dto.command.user.SignUpCommand
import com.stocksimulator.userservice.application.port.`in`.user.GetCurrentUserUseCase
import com.stocksimulator.userservice.application.port.`in`.user.LoginUseCase
import com.stocksimulator.userservice.application.port.`in`.user.SignUpUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 API")
class UserWebAdapter(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 회원가입 API
     * 
     * @param request 회원가입 요청 (이메일, 닉네임, 비밀번호)
     * @return 회원가입 결과 (사용자 ID, 이메일, 닉네임)
     */
    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 초기 잔고 5,000,000원이 자동으로 생성됩니다."
    )
    fun signUp(
        @Valid @RequestBody request: SignUpRequest
    ): Mono<ResponseEntity<ApiResponse<SignUpResponse>>> = mono {
        logger.info("회원가입 요청: email={}, username={}", request.email, request.username)
        
        // Request DTO → Command 변환
        val command = SignUpCommand(
            email = request.email,
            username = request.username,
            password = request.password
        )
        
        // UseCase 호출 (동기 방식)
        val result = signUpUseCase.signUp(command)
        
        // Result → Response 변환
        val response = SignUpResponse(
            userId = result.userId,
            email = result.email,
            username = result.username
        )
        
        logger.info("회원가입 완료: userId={}, email={}", result.userId, result.email)
        
        // ApiResponse 래핑 및 HTTP 201 Created 응답
        ApiResponse.created(response, "회원가입이 완료되었습니다").toCreatedResponseEntity()
    }

    /**
     * 로그인 API
     * 
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @param webSession WebFlux Session
     * @return 로그인 결과 (사용자 정보 + 세션 ID)
     */
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다. 세션이 생성되고 쿠키로 전달됩니다."
    )
    fun login(
        @Valid @RequestBody request: LoginRequest,
        webSession: WebSession
    ): Mono<ResponseEntity<ApiResponse<LoginResponse>>> = mono {
        logger.info("로그인 요청: email={}", request.email)

        // Request DTO → Command 변환
        val command = LoginCommand(
            email = request.email,
            password = request.password
        )

        // UseCase 호출 (동기 방식)
        val result = loginUseCase.login(command)

        // Session에 userId 저장
        webSession.attributes["userId"] = result.userId
        logger.info("세션 생성: sessionId={}, userId={}", webSession.id, result.userId)

        // Result → Response 변환
        val response = LoginResponse(
            userId = result.userId,
            email = result.email,
            username = result.username,
            role = result.role.name,
            sessionId = webSession.id
        )

        logger.info("로그인 완료: userId={}, sessionId={}", result.userId, webSession.id)

        // ApiResponse 래핑 및 HTTP 200 OK 응답
        ApiResponse.success(response, "로그인에 성공했습니다").toResponseEntity()
    }

    /**
     * 로그아웃 API
     * 
     * @param webSession WebFlux Session
     * @return 로그아웃 결과
     */
    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 무효화하고 로그아웃합니다."
    )
    fun logout(webSession: WebSession): Mono<ResponseEntity<ApiResponse<Void>>> {
        return webSession.invalidate().then(
            mono {
                logger.info("로그아웃 완료: sessionId={}", webSession.id)
                ApiResponse.success<Void>("로그아웃되었습니다").toResponseEntity()
            }
        )
    }

    /**
     * 현재 사용자 정보 조회 API
     * 
     * @param webSession WebFlux Session
     * @return 현재 사용자 정보
     */
    @GetMapping("/me")
    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "세션에 저장된 현재 로그인한 사용자의 정보를 조회합니다."
    )
    fun getCurrentUser(webSession: WebSession): Mono<ResponseEntity<ApiResponse<UserResponse>>> = mono {
        // Session에서 userId 추출
        val userId = webSession.getAttribute<Long>("userId")
            ?: run {
                logger.warn("인증되지 않은 요청: sessionId={}", webSession.id)
                return@mono ApiResponse.error<UserResponse>("인증이 필요합니다", "A001")
                    .toResponseEntity(HttpStatus.UNAUTHORIZED)
            }

        logger.info("현재 사용자 조회: userId={}", userId)

        // UseCase 호출 (동기 방식)
        val result = getCurrentUserUseCase.getCurrentUser(userId)

        // Result → Response 변환
        val response = UserResponse(
            userId = result.userId,
            email = result.email,
            username = result.username,
            role = result.role.name
        )

        logger.info("현재 사용자 조회 완료: userId={}", result.userId)

        // ApiResponse 래핑 및 HTTP 200 OK 응답
        ApiResponse.success(response).toResponseEntity()
    }
}
