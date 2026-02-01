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
        
        stage('🔨 Build Backend Services') {
            steps {
                script {
                    echo "🏗️ Building all backend services..."
                    sh '''
                        chmod +x gradlew
                        ./gradlew clean build -x test --no-daemon
                    '''
                }
            }
        }
        
        stage('🐳 Build & Push Docker Images') {
            parallel {
                stage('Eureka Server') {
                    steps {
                        script {
                            buildAndPush('eureka-server')
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        script {
                            buildAndPush('api-gateway')
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        script {
                            buildAndPush('user-service')
                        }
                    }
                }
                stage('Stock Service') {
                    steps {
                        script {
                            buildAndPush('stock-service')
                        }
                    }
                }
                stage('Trading Service') {
                    steps {
                        script {
                            buildAndPush('trading-service')
                        }
                    }
                }
                stage('Event Service') {
                    steps {
                        script {
                            buildAndPush('event-service')
                        }
                    }
                }
                stage('Scheduler Service') {
                    steps {
                        script {
                            buildAndPush('scheduler-service')
                        }
                    }
                }
                stage('News Service') {
                    steps {
                        script {
                            buildAndPush('news-service')
                        }
                    }
                }
                stage('Frontend') {
                    steps {
                        script {
                            buildAndPush('frontend', 'frontend')
                        }
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
                    
                    sh '''
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
                    '''
                }
            }
        }
        
        stage('🏥 Health Check') {
            steps {
                script {
                    echo "🏥 Running health checks..."
                    
                    sh '''
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
                    '''
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
            // Optionally trigger rollback
        }
        always {
            cleanWs()
        }
    }
}

def buildAndPush(String service, String context = null) {
    def serviceContext = context ?: "backend/${service}"
    def dockerFile = context ? "${context}/Dockerfile" : "backend/${service}/Dockerfile"
    
    echo "🐳 Building ${service}..."
    
    sh """
        docker login ${REGISTRY} -u \${DOCKER_CREDENTIALS_USR} -p \${DOCKER_CREDENTIALS_PSW}
        
        docker build -t ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} -f ${dockerFile} ${serviceContext}
        docker tag ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION} ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
        
        docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:${VERSION}
        docker push ${REGISTRY}/${IMAGE_PREFIX}/${service}:latest
        
        echo "✅ ${service} pushed successfully"
    """
}
