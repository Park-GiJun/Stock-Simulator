import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

version = "2025.11"

project {
    buildType(StockSimulatorDeploy)
}

object StockSimulatorDeploy : BuildType({
    name = "StockSimulatorDeploy"

    params {
        text("version", "", display = ParameterDisplay.NORMAL, label = "Version",
            description = "Auto-generated from git. Leave empty for auto (commit hash). Override for manual builds.")
        select("environment", "production", display = ParameterDisplay.NORMAL, label = "Environment", description = "Deployment environment",
            options = listOf("production", "staging"))
        select("buildTarget", "all", display = ParameterDisplay.NORMAL, label = "Build Target", description = "Build target selection",
            options = listOf("all", "frontend-only", "backend-only"))
        checkbox("cleanBuild", "false", display = ParameterDisplay.NORMAL, label = "Clean Build", description = "Clean build (ignore cache)",
            checked = "true", unchecked = "false")
        checkbox("skipBuild", "false", display = ParameterDisplay.NORMAL, label = "Skip Build", description = "Skip build (deploy images only)",
            checked = "true", unchecked = "false")
        text("env.REGISTRY", "ghcr.io", display = ParameterDisplay.HIDDEN)
        text("env.IMAGE_PREFIX", "park-gijun/stocksim", display = ParameterDisplay.HIDDEN)
        password("env.DOCKER_USER", "", display = ParameterDisplay.HIDDEN, label = "Docker Registry User")
        password("env.DOCKER_PASSWORD", "", display = ParameterDisplay.HIDDEN, label = "Docker Registry Password")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        // Step 0: Auto-generate version from git if not specified
        script {
            name = "Resolve Version"
            scriptContent = """
                #!/bin/bash
                if [ -z "%version%" ]; then
                    SHORT_SHA=${'$'}(git rev-parse --short HEAD)
                    BUILD_NUM="%build.counter%"
                    AUTO_VERSION="build-${'$'}{BUILD_NUM}-${'$'}{SHORT_SHA}"
                    echo "##teamcity[setParameter name='version' value='${'$'}AUTO_VERSION']"
                    echo "Auto-generated version: ${'$'}AUTO_VERSION"
                else
                    echo "Using specified version: %version%"
                fi
            """.trimIndent()
        }

        // Step 1: Gradle Build
        gradle {
            name = "Gradle Build"
            tasks = "build -x test --parallel"
            buildFile = "build.gradle.kts"
            conditions {
                equals("skipBuild", "false")
                doesNotEqual("buildTarget", "frontend-only")
            }
        }

        // Step 1b: Gradle Clean Build (when cleanBuild is true)
        gradle {
            name = "Gradle Clean Build"
            tasks = "clean build -x test --parallel"
            buildFile = "build.gradle.kts"
            conditions {
                equals("skipBuild", "false")
                equals("cleanBuild", "true")
                doesNotEqual("buildTarget", "frontend-only")
            }
        }

        // Step 2: Docker Login
        script {
            name = "Docker Login"
            scriptContent = """
                echo %env.DOCKER_PASSWORD% | docker login %env.REGISTRY% -u %env.DOCKER_USER% --password-stdin
            """.trimIndent()
            conditions {
                equals("skipBuild", "false")
            }
        }

        // Step 3: Build & Push Backend Images
        script {
            name = "Build & Push Backend Images"
            scriptContent = """
                #!/bin/bash
                set -e

                REGISTRY="%env.REGISTRY%"
                IMAGE_PREFIX="%env.IMAGE_PREFIX%"
                VERSION="%version%"

                SERVICES="eureka-server api-gateway user-service stock-service trading-service event-service scheduler-service news-service"

                for SERVICE in ${'$'}SERVICES; do
                    echo "Building ${'$'}SERVICE..."
                    cd backend/${'$'}SERVICE
                    docker build -t ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/${'$'}SERVICE:${'$'}VERSION .
                    docker tag ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/${'$'}SERVICE:${'$'}VERSION ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/${'$'}SERVICE:latest
                    docker push ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/${'$'}SERVICE:${'$'}VERSION
                    docker push ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/${'$'}SERVICE:latest
                    cd ../..
                    echo "${'$'}SERVICE done."
                done

                echo "All backend images built and pushed."
            """.trimIndent()
            conditions {
                equals("skipBuild", "false")
                doesNotEqual("buildTarget", "frontend-only")
            }
        }

        // Step 4: Build & Push Frontend Image
        script {
            name = "Build & Push Frontend Image"
            scriptContent = """
                #!/bin/bash
                set -e

                REGISTRY="%env.REGISTRY%"
                IMAGE_PREFIX="%env.IMAGE_PREFIX%"
                VERSION="%version%"

                echo "Building Frontend..."
                cd frontend
                docker build -t ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/frontend:${'$'}VERSION .
                docker tag ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/frontend:${'$'}VERSION ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/frontend:latest
                docker push ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/frontend:${'$'}VERSION
                docker push ${'$'}REGISTRY/${'$'}IMAGE_PREFIX/frontend:latest
                cd ..

                echo "Frontend image built and pushed."
            """.trimIndent()
            conditions {
                equals("skipBuild", "false")
                doesNotEqual("buildTarget", "backend-only")
            }
        }

        // Step 5: Deploy
        script {
            name = "Deploy to Production"
            scriptContent = """
                #!/bin/bash
                set -e

                REGISTRY="%env.REGISTRY%"
                VERSION="%version%"
                BUILD_TARGET="%buildTarget%"

                # Copy infrastructure configuration files to /deploy
                echo "Copying infrastructure and config files..."
                cp -rf docker-compose.yml /deploy/
                cp -rf infra /deploy/

                cd /deploy

                # Login to registry
                echo %env.DOCKER_PASSWORD% | docker login ${'$'}REGISTRY -u %env.DOCKER_USER% --password-stdin

                # Update .env file with version
                sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${'$'}VERSION/" .env || echo "IMAGE_TAG=${'$'}VERSION" >> .env

                # Pull new images
                docker-compose -p stock-simulator --profile all pull --ignore-pull-failures

                if [ "${'$'}BUILD_TARGET" = "frontend-only" ]; then
                    echo "Deploying Frontend only..."
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate frontend
                    echo "Frontend deployment complete"

                elif [ "${'$'}BUILD_TARGET" = "backend-only" ]; then
                    echo "Deploying Backend services only..."

                    # Eureka first
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate eureka-server
                    echo "Waiting for Eureka to start..."
                    for i in ${'$'}(seq 1 12); do
                        if curl -sf http://stockSimulator-eureka-server:8761/actuator/health > /dev/null 2>&1; then
                            echo "Eureka is ready!"
                            break
                        fi
                        echo "  Attempt ${'$'}i/12 - Eureka not ready yet, waiting 10s..."
                        sleep 10
                    done

                    # Backend services
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate user-service stock-service trading-service event-service scheduler-service news-service
                    sleep 15

                    # API Gateway
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate api-gateway
                    sleep 10

                    echo "Backend deployment complete"

                else
                    echo "Full deployment..."

                    # Clean up orphan containers
                    echo "Cleaning up orphan containers..."
                    docker rm -f ${'$'}(docker ps -aq --filter "name=stockSimulator-") 2>/dev/null || true

                    # 0. Infrastructure services first
                    docker-compose -p stock-simulator --profile all up -d postgres-primary postgres-replica redis mongodb kafka elasticsearch loki promtail prometheus grafana
                    echo "Waiting for infrastructure to be ready..."
                    sleep 20

                    # 1. Eureka first
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate eureka-server
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
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate user-service stock-service trading-service event-service scheduler-service news-service
                    sleep 15

                    # 3. API Gateway
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate api-gateway
                    sleep 10

                    # 4. Frontend
                    docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate frontend

                    echo "Full deployment complete"
                fi
            """.trimIndent()
            conditions {
                equals("environment", "production")
            }
        }

        // Step 6: Health Check
        script {
            name = "Health Check"
            scriptContent = """
                #!/bin/bash
                set -e

                echo "Waiting for services to stabilize..."
                sleep 15

                # Check Eureka
                EUREKA_OK=false
                for i in ${'$'}(seq 1 6); do
                    if curl -sf http://stockSimulator-eureka-server:8761/actuator/health > /dev/null 2>&1; then
                        echo "Eureka is healthy"
                        EUREKA_OK=true
                        break
                    fi
                    echo "  Eureka attempt ${'$'}i/6 - not ready, waiting 10s..."
                    sleep 10
                done
                if [ "${'$'}EUREKA_OK" = "false" ]; then
                    echo "Eureka health check failed after 6 attempts"
                    docker logs stockSimulator-eureka-server --tail 30 2>&1 || true
                    exit 1
                fi

                # Check API Gateway
                GATEWAY_OK=false
                for i in ${'$'}(seq 1 6); do
                    if curl -sf http://stockSimulator-api-gateway:8080/actuator/health > /dev/null 2>&1; then
                        echo "API Gateway is healthy"
                        GATEWAY_OK=true
                        break
                    fi
                    echo "  Gateway attempt ${'$'}i/6 - not ready, waiting 10s..."
                    sleep 10
                done
                if [ "${'$'}GATEWAY_OK" = "false" ]; then
                    echo "API Gateway health check failed after 6 attempts"
                    docker logs stockSimulator-api-gateway --tail 30 2>&1 || true
                    exit 1
                fi

                echo "All health checks passed!"
            """.trimIndent()
            conditions {
                equals("environment", "production")
                doesNotEqual("buildTarget", "frontend-only")
            }
        }
    }

    triggers {
        vcs {
            branchFilter = "+:refs/heads/master"
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_1"
            }
        }
    }

    maxRunningBuilds = 1

    failureConditions {
        executionTimeoutMin = 30
    }
})
