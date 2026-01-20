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

    // JPA & MySQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Kotlin JDSL
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.4")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.4")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.4")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.4")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
