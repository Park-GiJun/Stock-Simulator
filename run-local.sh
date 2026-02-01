#!/bin/bash

# =============================================================================
# 로컬 개발 환경 실행 스크립트
# =============================================================================
# 인프라(DB, Redis, Kafka)는 원격 서버 사용
# 백엔드 서비스만 로컬에서 실행

echo "=================================="
echo "Stock Simulator - Local Development"
echo "=================================="
echo ""

# .env.local을 .env로 복사
echo "1. Setting up environment variables..."
cp .env.local .env
echo "✓ Using remote infrastructure (172.30.1.79)"
echo ""

# Gradle 빌드
echo "2. Building backend services..."
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "✗ Build failed!"
    exit 1
fi
echo "✓ Build completed"
echo ""

# Docker Compose로 백엔드 서비스만 시작
echo "3. Starting backend services..."
docker-compose up -d \
    eureka-server \
    api-gateway \
    user-service \
    stock-service \
    trading-service \
    event-service \
    scheduler-service \
    news-service

if [ $? -ne 0 ]; then
    echo "✗ Failed to start services!"
    exit 1
fi
echo "✓ Services started"
echo ""

echo "4. Waiting for services to be ready (60 seconds)..."
sleep 60
echo ""

echo "5. Checking service status..."
docker-compose ps
echo ""

echo "=================================="
echo "✓ Local development environment ready!"
echo "=================================="
echo ""
echo "Services:"
echo "  - Eureka:      http://localhost:8761"
echo "  - API Gateway: http://localhost:9832"
echo "  - User:        http://localhost:8081"
echo "  - Stock:       http://localhost:8082"
echo "  - Trading:     http://localhost:8083"
echo "  - Event:       http://localhost:8084"
echo "  - Scheduler:   http://localhost:8085"
echo "  - News:        http://localhost:8086"
echo ""
echo "Infrastructure (Remote):"
echo "  - PostgreSQL:      172.30.1.79:5432"
echo "  - Redis:           172.30.1.79:6380"
echo "  - MongoDB:         172.30.1.79:27018"
echo "  - Kafka:           172.30.1.79:9093"
echo "  - Elasticsearch:   172.30.1.79:9201"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f [service-name]"
echo ""
echo "To stop:"
echo "  docker-compose down"
