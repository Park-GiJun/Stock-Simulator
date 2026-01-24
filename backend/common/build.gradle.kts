plugins {
    kotlin("plugin.jpa")
    kotlin("plugin.serialization")
}

// common 모듈은 bootJar 비활성화, jar만 생성
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Kotlin JDSL
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.4")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.4")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.4")

    // Kotlinx Serialization (for shared DTOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Redisson
    implementation("org.redisson:redisson:3.52.0")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Swagger / OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.4")
}
