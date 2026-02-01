dependencies {
    implementation(project(":backend:common"))
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // CompileOnly for autoconfiguration exclusion
    compileOnly("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.6.0")
}
