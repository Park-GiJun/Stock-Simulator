plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":backend:common"))

    // Spring WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // JPA & PostgreSQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Kotlin JDSL
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.4")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.4")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.4")

    // Spring Session (Redis - WebFlux)
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.1")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
}
