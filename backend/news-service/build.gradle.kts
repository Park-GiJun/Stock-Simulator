dependencies {
    implementation(project(":backend:common"))

    // Spring WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // JPA + PostgreSQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Elasticsearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.4")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
