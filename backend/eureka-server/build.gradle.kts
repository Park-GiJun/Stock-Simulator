dependencies {
    implementation(project(":backend:common"))
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
