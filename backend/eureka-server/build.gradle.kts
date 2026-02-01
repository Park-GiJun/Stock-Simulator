dependencies {
    implementation(project(":backend:common"))
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // JPA & Kotlin JDSL (to prevent autoconfiguration errors)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.6.0")
    runtimeOnly("org.postgresql:postgresql")
}
