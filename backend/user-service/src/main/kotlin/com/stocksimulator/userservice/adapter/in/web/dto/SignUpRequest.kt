package com.stocksimulator.userservice.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청")
data class SignUpRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    @Schema(description = "이메일", example = "user@example.com", required = true)
    val email: String,

    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9가-힣]+$",
        message = "닉네임은 영문, 한글, 숫자만 가능합니다"
    )
    @Schema(description = "닉네임", example = "테스트유저", required = true)
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "Test1234!", required = true)
    val password: String
)
