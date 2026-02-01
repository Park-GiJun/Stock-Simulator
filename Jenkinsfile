pipeline {
    agent any
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    environment {
        REGISTRY = 'ghcr.io'
        IMAGE_PREFIX = 'park-gijun/stocksim'
        DOCKER_CREDENTIALS = credentials('github-token')
    }
    
    parameters {
        string(name: 'VERSION', defaultValue: 'v1.4.1', description: 'Version to deploy (e.g., v1.4.1)')
        choice(name: 'ENVIRONMENT', choices: ['production', 'staging'], description: 'Deployment environment')
        booleanParam(name: 'CLEAN_BUILD', defaultValue: false, description: '클린 빌드 (캐시 무시)')
        booleanParam(name: 'SKIP_BUILD', defaultValue: false, description: '빌드 스킵 (이미지만 배포)')
    }
    
    stages {
        stage('🔍 Checkout') {
            steps {
                echo "📥 Checking out code..."
                checkout scm
            }
        }
        
        stage('🔨 Build with Gradle') {
            when {
                expression { !params.SKIP_BUILD }
            }
            steps {
                script {
                    def buildCmd = params.CLEAN_BUILD ? 'clean build' : 'build'
                    echo "🏗️ Building all backend services (clean: ${params.CLEAN_BUILD})..."
                    sh """
                        chmod +x gradlew
                        ./gradlew ${buildCmd} -x test --no-daemon --build-cache --parallel
                    """
                }
            }
        }
        
        stage('🐳 Build & Push Docker Images') {
            when {
                expression { !params.SKIP_BUILD }
            }
            steps {
                script {
                    echo "🐳 Building and pushing Docker images..."
                    
                    sh """
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login ${REGISTRY} -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                    """
                    
                    def services = ['eureka-server', 'api-gateway', 'user-service', 'stock-service', 'trading-service', 'event-service', 'scheduler-service', 'news-service']
                    
                    // 병렬로 Docker 빌드
                    def parallelStages = [:]
                    
                    services.each { service ->
                        parallelStages[service] = {
                            sh """
                                cd backend/${service}
                                docker build -t ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} .
                                docker tag ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
                                docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION}
                                docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
                                cd ../..
                            """
                        }
                    }
                    
                    parallelStages['frontend'] = {
                        sh """
                            cd frontend
                            docker build -t ${REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION} .
                            docker tag ${REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION} ${REGISTRY}/${IMAGE_PREFIX}/frontend:latest
                            docker push ${REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION}
                            docker push ${REGISTRY}/${IMAGE_PREFIX}/frontend:latest
                            cd ..
                        """
                    }
                    
                    parallel parallelStages
                }
            }
        }
        
        stage('🚀 Deploy to Production') {
            when {
                expression { params.ENVIRONMENT == 'production' }
            }
            steps {
                script {
                    echo "🚀 Deploying to production..."
                    
                    sh """
                        # Copy infrastructure configuration files to /deploy
                        echo "📋 Copying infrastructure and config files..."
                        cp -rf docker-compose.yml /deploy/
                        cp -rf infra /deploy/
                        
                        cd /deploy
                        
                        # Login to registry
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login ${REGISTRY} -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                        
                        # Update .env file with version
                        sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${VERSION}/" .env || echo "IMAGE_TAG=${VERSION}" >> .env
                        
                        # Pull new images (ignore errors for local-build images)
                        docker-compose -p stock-simulator --profile all pull --ignore-pull-failures
                        
                        # 기존 고아 컨테이너 정리 (이전 배포 실패 시 남은 컨테이너 제거)
                        echo "🧹 Cleaning up orphan containers..."
                        docker rm -f \$(docker ps -aq --filter "name=stockSimulator-") 2>/dev/null || true
                        
                        # Rolling update
                        echo "🔄 Starting rolling update..."
                        
                        # 0. 인프라 서비스 먼저 기동 (DB, Redis, Kafka 등)
                        docker-compose -p stock-simulator --profile all up -d postgres-primary postgres-replica redis mongodb kafka zookeeper elasticsearch loki promtail prometheus grafana
                        echo "⏳ Waiting for infrastructure to be ready..."
                        sleep 20
                        
                        # 1. Eureka first
                        docker-compose -p stock-simulator --profile all up -d --no-deps --force-recreate eureka-server
                        echo "⏳ Waiting for Eureka to start..."
                        for i in \$(seq 1 12); do
                            if curl -sf http://stockSimulator-eureka-server:8761/actuator/health > /dev/null 2>&1; then
                                echo "✅ Eureka is ready!"
                                break
                            fi
                            echo "  Attempt \$i/12 - Eureka not ready yet, waiting 10s..."
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
                        
                        echo "✅ Deployment complete"
                    """
                }
            }
        }
        
        stage('🏥 Health Check') {
            when {
                expression { params.ENVIRONMENT == 'production' }
            }
            steps {
                script {
                    echo "🏥 Running health checks..."
                    
                    sh """
                        echo "⏳ Waiting for services to stabilize..."
                        sleep 15
                        
                        # Check Eureka (retry up to 6 times, 10s interval)
                        EUREKA_OK=false
                        for i in \$(seq 1 6); do
                            if curl -sf http://stockSimulator-eureka-server:8761/actuator/health > /dev/null 2>&1; then
                                echo "✅ Eureka is healthy"
                                EUREKA_OK=true
                                break
                            fi
                            echo "  Eureka attempt \$i/6 - not ready, waiting 10s..."
                            sleep 10
                        done
                        if [ "\$EUREKA_OK" = "false" ]; then
                            echo "❌ Eureka health check failed after 6 attempts"
                            docker logs stockSimulator-eureka-server --tail 30 2>&1 || true
                            exit 1
                        fi
                        
                        # Check API Gateway (retry up to 6 times, 10s interval)
                        GATEWAY_OK=false
                        for i in \$(seq 1 6); do
                            if curl -sf http://stockSimulator-api-gateway:8080/actuator/health > /dev/null 2>&1; then
                                echo "✅ API Gateway is healthy"
                                GATEWAY_OK=true
                                break
                            fi
                            echo "  Gateway attempt \$i/6 - not ready, waiting 10s..."
                            sleep 10
                        done
                        if [ "\$GATEWAY_OK" = "false" ]; then
                            echo "❌ API Gateway health check failed after 6 attempts"
                            docker logs stockSimulator-api-gateway --tail 30 2>&1 || true
                            exit 1
                        fi
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo "🎉 Pipeline completed successfully!"
            echo "📦 Version: ${params.VERSION}"
            echo "🌐 Environment: ${params.ENVIRONMENT}"
        }
        failure {
            echo "❌ Pipeline failed!"
        }
    }
}
