dependencies {
    implementation(project(":backend:common"))

    // Spring WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Scheduling
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
