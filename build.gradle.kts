plugins {
    kotlin("multiplatform") version "1.9.25" apply false
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.serialization") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.stocksimulator"
    version = "0.0.1-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
