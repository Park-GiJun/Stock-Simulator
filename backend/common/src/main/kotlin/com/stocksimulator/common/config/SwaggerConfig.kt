package com.stocksimulator.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 공통 Swagger/OpenAPI 설정
 *
 * 각 서비스에서 이 설정을 상속하거나 직접 사용할 수 있습니다.
 * application.yml에서 springdoc 관련 설정을 추가하면 됩니다.
 */
@Configuration
class SwaggerConfig(
    @Value("\${spring.application.name:service}")
    private val applicationName: String,

    @Value("\${server.port:8080}")
    private val serverPort: Int,

    @Value("\${springdoc.version:1.0.0}")
    private val apiVersion: String = "1.0.0"
) {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(apiInfo())
            .servers(listOf(
                Server()
                    .url("http://localhost:$serverPort")
                    .description("Local Development Server"),
                Server()
                    .url("http://localhost:8080")
                    .description("API Gateway")
            ))
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사 없이 토큰만 입력합니다.")
                    )
            )
    }

    private fun apiInfo(): Info {
        return Info()
            .title("Stock Simulator - ${formatServiceName(applicationName)}")
            .description("""
                |## 📈 모의 주식 게임 API
                |
                |AI 기반 이벤트 + 수급 기반 주가 변동 모의 주식 시뮬레이션 서비스입니다.
                |
                |### 주요 기능
                |- 실시간 주식 시세 조회
                |- 매수/매도 주문
                |- 포트폴리오 관리
                |- 시즌별 랭킹 시스템
                |
                |### 인증
                |대부분의 API는 JWT 토큰 인증이 필요합니다.
                |로그인 후 발급받은 토큰을 `Authorization` 헤더에 포함하세요.
                |
                |```
                |Authorization: Bearer {your-jwt-token}
                |```
            """.trimMargin())
            .version(apiVersion)
            .contact(
                Contact()
                    .name("Stock Simulator Team")
                    .email("support@stocksimulator.com")
                    .url("https://github.com/stocksimulator")
            )
            .license(
                License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
            )
    }

    private fun formatServiceName(name: String): String {
        return name.split("-")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

/**
 * Swagger 그룹 설정을 위한 프로퍼티 클래스
 */
data class SwaggerGroupConfig(
    val group: String,
    val pathsToMatch: List<String>,
    val displayName: String
)
