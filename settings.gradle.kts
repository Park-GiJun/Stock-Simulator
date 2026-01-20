rootProject.name = "stock-simulator"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Backend modules
include(":backend")
include(":backend:common")
include(":backend:eureka-server")
include(":backend:api-gateway")
include(":backend:user-service")
include(":backend:stock-service")
include(":backend:trading-service")
include(":backend:event-service")
include(":backend:scheduler-service")
include(":backend:news-service")
include(":backend:season-service")
