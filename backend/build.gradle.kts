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
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        // Kotlin
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

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
        "testImplementation"("io.mockk:mockk:1.13.8")
        "testImplementation"("com.ninja-squad:springmockk:4.0.2")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
