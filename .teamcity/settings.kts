import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

version = "2025.11"

project {
    buildType(StockSimulatorDeploy)
}

object StockSimulatorDeploy : BuildType({
    name = "StockSimulatorDeploy"

    maxRunningBuilds = 1

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        // Step 1: Gradle Build
        gradle {
            name = "Gradle Build"
            tasks = "clean build -x test --parallel"
            useGradleWrapper = true
        }

        // Step 2: Build Backend Docker Images (local only)
        script {
            name = "Build Backend Images"
            scriptContent = """
                #!/bin/bash
                set -e

                IMAGE_PREFIX="stocksim"

                for SERVICE in eureka-server api-gateway user-service stock-service trading-service event-service scheduler-service; do
                    echo "Building ${'$'}SERVICE..."
                    cd backend/${'$'}SERVICE
                    docker build --no-cache -t ${'$'}IMAGE_PREFIX/${'$'}SERVICE:latest .
                    cd ../..
                    echo "${'$'}SERVICE done."
                done

                echo "All backend images built."
            """.trimIndent()
        }

        // Step 3: Build Frontend Docker Image (local only)
        script {
            name = "Build Frontend Image"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "Building Frontend..."
                cd frontend
                docker build --no-cache -t stocksim/frontend:latest .
                cd ..

                echo "Frontend image built."
            """.trimIndent()
        }

        // Step 4: Deploy
        script {
            name = "Deploy"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "Copying infrastructure and config files..."
                cp -rf docker-compose.yml /deploy/
                cp -rf infra /deploy/

                cd /deploy

                # Ensure DOCKER_REGISTRY uses local images
                sed -i 's|^DOCKER_REGISTRY=.*|DOCKER_REGISTRY=stocksim|' /deploy/.env || true

                echo "Full deployment..."

                # 1. Eureka first
                docker compose -p stock-simulator --profile all up -d --no-deps --force-recreate eureka-server
                echo "Waiting for Eureka to start..."
                for i in ${'$'}(seq 1 12); do
                    if curl -sf http://stockSimulator-eureka-server:8761/actuator/health > /dev/null 2>&1; then
                        echo "Eureka is ready!"
                        break
                    fi
                    echo "  Attempt ${'$'}i/12 - Eureka not ready yet, waiting 10s..."
                    sleep 10
                done

                # 2. Backend services
                docker compose -p stock-simulator --profile all up -d --no-deps --force-recreate user-service stock-service trading-service event-service scheduler-service
                sleep 15

                # 3. API Gateway
                docker compose -p stock-simulator --profile all up -d --no-deps --force-recreate api-gateway
                sleep 10

                # 4. Frontend
                docker compose -p stock-simulator --profile all up -d --no-deps --force-recreate frontend

                echo "Deployment complete"
            """.trimIndent()
        }

        // Step 5: Health Check
        script {
            name = "Health Check"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "Waiting for services to stabilize..."
                sleep 15

                # Check Eureka
                for i in ${'$'}(seq 1 6); do
                    if curl -sf http://stockSimulator-eureka-server:8761/actuator/health > /dev/null 2>&1; then
                        echo "Eureka is healthy"
                        break
                    fi
                    if [ "${'$'}i" = "6" ]; then
                        echo "Eureka health check failed"
                        exit 1
                    fi
                    echo "  Eureka attempt ${'$'}i/6 - waiting 10s..."
                    sleep 10
                done

                # Check API Gateway
                for i in ${'$'}(seq 1 6); do
                    if curl -sf http://stockSimulator-api-gateway:8080/actuator/health > /dev/null 2>&1; then
                        echo "API Gateway is healthy"
                        break
                    fi
                    if [ "${'$'}i" = "6" ]; then
                        echo "API Gateway health check failed"
                        exit 1
                    fi
                    echo "  Gateway attempt ${'$'}i/6 - waiting 10s..."
                    sleep 10
                done

                echo "All health checks passed!"
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            branchFilter = "+:refs/heads/master"
        }
    }

    failureConditions {
        executionTimeoutMin = 30
    }
})
