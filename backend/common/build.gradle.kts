plugins {
    kotlin("plugin.serialization")
}

// common 모듈은 bootJar 비활성화, jar만 생성
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlinx Serialization (for shared DTOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // Kafka (for shared events)
    implementation("org.springframework.kafka:spring-kafka")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Swagger / OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.1")
    
    // Logback for structured logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
}
