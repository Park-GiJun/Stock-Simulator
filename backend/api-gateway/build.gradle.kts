dependencies {
    implementation(project(":backend:common"))

    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Redis for session validation
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // MongoDB removed - API Gateway doesn't need database, only routes requests
    // implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Swagger / OpenAPI for Gateway
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.4")
}
