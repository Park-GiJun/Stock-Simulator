pipeline {
    agent any
    
    environment {
        REGISTRY = 'ghcr.io'
        IMAGE_PREFIX = 'park-gijun/stocksim'
        DOCKER_CREDENTIALS = credentials('github-token')
        DEPLOY_PATH = '/home/gijunpark/Stock-Simulator'
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
        
        stage('🐳 Build & Push Docker Images - Direct') {
            steps {
                script {
                    echo "🏗️ Building Docker images directly (no Gradle pre-build)..."
                    
                    sh """
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login ${REGISTRY} -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                    """
                    
                    def services = [
                        'eureka-server': 'backend/eureka-server',
                        'api-gateway': 'backend/api-gateway',
                        'user-service': 'backend/user-service',
                        'stock-service': 'backend/stock-service',
                        'trading-service': 'backend/trading-service',
                        'event-service': 'backend/event-service',
                        'scheduler-service': 'backend/scheduler-service',
                        'news-service': 'backend/news-service',
                        'frontend': 'frontend'
                    ]
                    
                    services.each { service, context ->
                        echo "🐳 Building ${service}..."
                        sh """
                            docker build -t ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} -f ${context}/Dockerfile ${context}
                            docker tag ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
                            docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION}
                            docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
                            echo "✅ ${service} pushed successfully"
                        """
                    }
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
                        cd ${DEPLOY_PATH}
                        
                        # Update .env file
                        sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${VERSION}/" .env || echo "IMAGE_TAG=${VERSION}" >> .env
                        
                        # Pull new images
                        docker-compose --profile all pull
                        
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
