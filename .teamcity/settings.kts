import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2025.11"

project {

    buildType(StockSimulatorDeploy)
}

object StockSimulatorDeploy : BuildType({
    name = "StockSimulatorDeploy"

    maxRunningBuilds = 1

    params {
        password("env.DOCKER_PASSWORD", "zxx775d03cbe80d301b", label = "Docker Registry Password", display = ParameterDisplay.HIDDEN)
        text("env.IMAGE_PREFIX", "park-gijun/stocksim", display = ParameterDisplay.HIDDEN)
        select("environment", "production", label = "Environment", description = "Deployment environment", display = ParameterDisplay.NORMAL,
                options = listOf("production", "staging"))
        checkbox("cleanBuild", "false", label = "Clean Build", description = "Clean build (ignore cache)", display = ParameterDisplay.NORMAL,
                  checked = "true", unchecked = "false")
        text("env.REGISTRY", "ghcr.io", display = ParameterDisplay.HIDDEN)
        text("version", "", label = "Version", description = "Auto-generated from git commit. Leave empty for auto.", display = ParameterDisplay.NORMAL)
        checkbox("skipBuild", "false", label = "Skip Build", description = "Skip build (deploy images only)", display = ParameterDisplay.NORMAL,
                  checked = "true", unchecked = "false")
        select("buildTarget", "all", label = "Build Target", description = "Build target selection", display = ParameterDisplay.NORMAL,
                options = listOf("all", "frontend-only", "backend-only"))
        password("env.DOCKER_USER", "zxx775d03cbe80d301b", label = "Docker Registry User", display = ParameterDisplay.HIDDEN)
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
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
        gradle {
            name = "Gradle Build"

            conditions {
                equals("skipBuild", "false")
                doesNotEqual("buildTarget", "frontend-only")
            }
            tasks = "build -x test --parallel"
            buildFile = "build.gradle.kts"
        }
        gradle {
            name = "Gradle Clean Build"

            conditions {
                equals("skipBuild", "false")
                equals("cleanBuild", "true")
                doesNotEqual("buildTarget", "frontend-only")
            }
            tasks = "clean build -x test --parallel"
            buildFile = "build.gradle.kts"
        }
        script {
            name = "Docker Login"

            conditions {
                equals("skipBuild", "false")
            }
            scriptContent = "echo %env.DOCKER_PASSWORD% | docker login %env.REGISTRY% -u %env.DOCKER_USER% --password-stdin"
        }
        script {
            name = "Build & Push Backend Images"

            conditions {
                equals("skipBuild", "false")
                doesNotEqual("buildTarget", "frontend-only")
            }
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
        }
        script {
            name = "Build & Push Frontend Image"

            conditions {
                equals("skipBuild", "false")
                doesNotEqual("buildTarget", "backend-only")
            }
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
        }
        script {
            name = "Deploy to Production"

            conditions {
                equals("environment", "production")
            }
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
                echo "${'$'}DOCKER_PASSWORD" | docker login ${'$'}REGISTRY -u "${'$'}DOCKER_USER" --password-stdin
                
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
        }
        script {
            name = "Health Check"

            conditions {
                equals("environment", "production")
                doesNotEqual("buildTarget", "frontend-only")
            }
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

    features {
        dockerRegistryConnections {}
    }
})
