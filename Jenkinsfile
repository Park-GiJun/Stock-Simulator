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
                        cd /deploy
                        
                        # Login to registry
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login ${REGISTRY} -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                        
                        # Update .env file with version
                        sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${VERSION}/" .env || echo "IMAGE_TAG=${VERSION}" >> .env
                        
                        # Pull new images (ignore errors for local-build images)
                        docker-compose --profile all pull --ignore-pull-failures
                        
                        # Rolling update
                        echo "🔄 Starting rolling update..."
                        
                        # 1. Eureka first
                        docker-compose --profile all up -d --no-deps --force-recreate eureka-server
                        sleep 15
                        
                        # 2. Backend services
                        docker-compose --profile all up -d --no-deps --force-recreate user-service stock-service trading-service event-service scheduler-service news-service
                        sleep 10
                        
                        # 3. API Gateway
                        docker-compose --profile all up -d --no-deps --force-recreate api-gateway
                        sleep 10
                        
                        # 4. Frontend
                        docker-compose --profile all up -d --no-deps --force-recreate frontend
                        
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
                        sleep 30
                        
                        # Check Eureka
                        if curl -sf http://localhost:8761/actuator/health; then
                            echo "\\n✅ Eureka is healthy"
                        else
                            echo "❌ Eureka health check failed"
                            exit 1
                        fi
                        
                        # Check API Gateway
                        if curl -sf http://localhost:9832/actuator/health; then
                            echo "\\n✅ API Gateway is healthy"
                        else
                            echo "❌ API Gateway health check failed"
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
