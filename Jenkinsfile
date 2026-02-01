pipeline {
    agent any
    
    environment {
        REGISTRY = 'ghcr.io'
        IMAGE_PREFIX = 'park-gijun/stocksim'
        DOCKER_CREDENTIALS = credentials('github-token')
    }
    
    parameters {
        string(name: 'VERSION', defaultValue: 'v1.4.1', description: 'Version to deploy (e.g., v1.4.1)')
        choice(name: 'ENVIRONMENT', choices: ['production', 'staging'], description: 'Deployment environment')
    }
    
    stages {
        stage('🔍 Checkout') {
            steps {
                echo "📥 Checking out code..."
                checkout scm
            }
        }
        
        stage('🔨 Build with Gradle') {
            steps {
                script {
                    echo "🏗️ Building all backend services with Gradle..."
                    sh '''
                        chmod +x gradlew
                        ./gradlew clean build -x test --no-daemon
                    '''
                }
            }
        }
        
        stage('🐳 Build & Push Docker Images') {
            steps {
                script {
                    echo "🐳 Building and pushing Docker images..."
                    
                    sh """
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login ${REGISTRY} -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                    """
                    
                    def services = ['eureka-server', 'api-gateway', 'user-service', 'stock-service', 'trading-service', 'event-service', 'scheduler-service', 'news-service']
                    
                    services.each { service ->
                        echo "🐳 Building ${service}..."
                        sh """
                            cd backend/${service}
                            docker build -t ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} .
                            docker tag ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
                            docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION}
                            docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
                            cd ../..
                        """
                    }
                    
                    // Frontend
                    echo "🐳 Building frontend..."
                    sh """
                        cd frontend
                        docker build -t ${REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION} .
                        docker tag ${REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION} ${REGISTRY}/${IMAGE_PREFIX}/frontend:latest
                        docker push ${REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION}
                        docker push ${REGISTRY}/${IMAGE_PREFIX}/frontend:latest
                        cd ..
                    """
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
                        
                        # Update .env file with version
                        sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${VERSION}/" .env || echo "IMAGE_TAG=${VERSION}" >> .env
                        
                        # Pull new images
                        docker compose --profile all pull
                        
                        # Rolling update
                        echo "🔄 Starting rolling update..."
                        
                        # 1. Eureka first
                        docker compose --profile all up -d --no-deps --force-recreate eureka-server
                        sleep 15
                        
                        # 2. Backend services
                        docker compose --profile all up -d --no-deps --force-recreate user-service stock-service trading-service event-service scheduler-service news-service
                        sleep 10
                        
                        # 3. API Gateway
                        docker compose --profile all up -d --no-deps --force-recreate api-gateway
                        sleep 10
                        
                        # 4. Frontend
                        docker compose --profile all up -d --no-deps --force-recreate frontend
                        
                        echo "✅ Deployment complete"
                    """
                }
            }
        }
        
        stage('🏥 Health Check') {
            steps {
                script {
                    echo "🏥 Running health checks..."
                    
                    sh """
                        sleep 30
                        
                        # Check Eureka
                        if curl -f http://localhost:8761/actuator/health; then
                            echo "✅ Eureka is healthy"
                        else
                            echo "❌ Eureka health check failed"
                            exit 1
                        fi
                        
                        # Check API Gateway
                        if curl -f http://localhost:9832/actuator/health; then
                            echo "✅ API Gateway is healthy"
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
