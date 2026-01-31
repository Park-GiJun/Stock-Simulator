@file:Suppress("GrazieInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Backend 모듈 공통 설정
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        // Kotlin
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
        "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json") // BOM managed by Spring Boot 4.0

        // Jackson
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        // dotenv
        "implementation"("me.paulschwarz:spring-dotenv:4.0.0")

        // Logging
        "implementation"("io.github.microutils:kotlin-logging-jvm:3.0.5")

        // Test
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
        "testImplementation"("io.mockk:mockk:1.13.12")
        "testImplementation"("com.ninja-squad:springmockk:4.0.2")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            freeCompilerArgs.add("-Xannotation-default-target=param-property")  // Kotlin 2.2.x 권장 설정
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)  // Kotlin 2.3.0 - Java 25 지원
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
