dependencies {
    implementation(project(":backend:common"))
    
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // MongoDB (for logging)
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Swagger / OpenAPI for Gateway
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.4")
}
