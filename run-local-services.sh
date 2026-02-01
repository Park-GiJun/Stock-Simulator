#!/bin/bash

# =============================================================================
# 로컬 개발 환경 실행 스크립트 (Linux/Mac)
# =============================================================================
# 서버 인프라(210.121.177.150) 사용, 백엔드 서비스만 로컬 실행

echo "=================================="
echo "Stock Simulator - Local Development"
echo "=================================="
echo ""

# 1. Eureka Server 실행
echo "[1/8] Starting Eureka Server..."
./gradlew :backend:eureka-server:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8761 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin" \
    > logs/eureka-server.log 2>&1 &
EUREKA_PID=$!
echo "Eureka PID: $EUREKA_PID"

sleep 20
echo "[OK] Eureka Server started on http://localhost:8761"
echo ""

# 2. API Gateway 실행
echo "[2/8] Starting API Gateway..."
./gradlew :backend:api-gateway:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8080 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin" \
    > logs/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
echo "API Gateway PID: $API_GATEWAY_PID"

sleep 15
echo "[OK] API Gateway started on http://localhost:8080"
echo ""

# 3. User Service 실행
echo "[3/8] Starting User Service..."
./gradlew :backend:user-service:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8081 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=users \
    --spring.datasource.username=stocksim \
    --spring.datasource.password=stocksim123 \
    --spring.data.redis.host=210.121.177.150 \
    --spring.data.redis.port=6380 \
    --spring.data.redis.password=stocksim123 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin \
    --spring.kafka.bootstrap-servers=210.121.177.150:9093" \
    > logs/user-service.log 2>&1 &
USER_SERVICE_PID=$!
echo "User Service PID: $USER_SERVICE_PID"

sleep 10
echo "[OK] User Service started on http://localhost:8081"
echo ""

# 4. Stock Service 실행
echo "[4/8] Starting Stock Service..."
./gradlew :backend:stock-service:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8082 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=stocks \
    --spring.datasource.username=stocksim \
    --spring.datasource.password=stocksim123 \
    --spring.data.redis.host=210.121.177.150 \
    --spring.data.redis.port=6380 \
    --spring.data.redis.password=stocksim123 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin \
    --spring.kafka.bootstrap-servers=210.121.177.150:9093" \
    > logs/stock-service.log 2>&1 &
STOCK_SERVICE_PID=$!
echo "Stock Service PID: $STOCK_SERVICE_PID"

sleep 10
echo "[OK] Stock Service started on http://localhost:8082"
echo ""

# 5. Trading Service 실행
echo "[5/8] Starting Trading Service..."
./gradlew :backend:trading-service:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8083 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=trading \
    --spring.datasource.username=stocksim \
    --spring.datasource.password=stocksim123 \
    --spring.data.redis.host=210.121.177.150 \
    --spring.data.redis.port=6380 \
    --spring.data.redis.password=stocksim123 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin \
    --spring.kafka.bootstrap-servers=210.121.177.150:9093" \
    > logs/trading-service.log 2>&1 &
TRADING_SERVICE_PID=$!
echo "Trading Service PID: $TRADING_SERVICE_PID"

sleep 10
echo "[OK] Trading Service started on http://localhost:8083"
echo ""

# 6. Event Service 실행
echo "[6/8] Starting Event Service..."
./gradlew :backend:event-service:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8084 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=events \
    --spring.datasource.username=stocksim \
    --spring.datasource.password=stocksim123 \
    --spring.data.redis.host=210.121.177.150 \
    --spring.data.redis.port=6380 \
    --spring.data.redis.password=stocksim123 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin \
    --spring.kafka.bootstrap-servers=210.121.177.150:9093" \
    > logs/event-service.log 2>&1 &
EVENT_SERVICE_PID=$!
echo "Event Service PID: $EVENT_SERVICE_PID"

sleep 10
echo "[OK] Event Service started on http://localhost:8084"
echo ""

# 7. Scheduler Service 실행
echo "[7/8] Starting Scheduler Service..."
./gradlew :backend:scheduler-service:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8085 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=scheduler \
    --spring.datasource.username=stocksim \
    --spring.datasource.password=stocksim123 \
    --spring.data.redis.host=210.121.177.150 \
    --spring.data.redis.port=6380 \
    --spring.data.redis.password=stocksim123 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin \
    --spring.kafka.bootstrap-servers=210.121.177.150:9093" \
    > logs/scheduler-service.log 2>&1 &
SCHEDULER_SERVICE_PID=$!
echo "Scheduler Service PID: $SCHEDULER_SERVICE_PID"

sleep 10
echo "[OK] Scheduler Service started on http://localhost:8085"
echo ""

# 8. News Service 실행
echo "[8/8] Starting News Service..."
./gradlew :backend:news-service:bootRun \
    --args="--spring.profiles.active=local \
    --server.port=8086 \
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=news \
    --spring.datasource.username=stocksim \
    --spring.datasource.password=stocksim123 \
    --spring.data.redis.host=210.121.177.150 \
    --spring.data.redis.port=6380 \
    --spring.data.redis.password=stocksim123 \
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin \
    --spring.kafka.bootstrap-servers=210.121.177.150:9093" \
    > logs/news-service.log 2>&1 &
NEWS_SERVICE_PID=$!
echo "News Service PID: $NEWS_SERVICE_PID"

sleep 10
echo "[OK] News Service started on http://localhost:8086"
echo ""

# PID 저장
mkdir -p logs
echo $EUREKA_PID > logs/eureka-server.pid
echo $API_GATEWAY_PID > logs/api-gateway.pid
echo $USER_SERVICE_PID > logs/user-service.pid
echo $STOCK_SERVICE_PID > logs/stock-service.pid
echo $TRADING_SERVICE_PID > logs/trading-service.pid
echo $EVENT_SERVICE_PID > logs/event-service.pid
echo $SCHEDULER_SERVICE_PID > logs/scheduler-service.pid
echo $NEWS_SERVICE_PID > logs/news-service.pid

echo "=================================="
echo "[OK] All services started!"
echo "=================================="
echo ""
echo "Local Services:"
echo "  - Eureka Server:      http://localhost:8761"
echo "  - API Gateway:        http://localhost:8080"
echo "  - User Service:       http://localhost:8081"
echo "  - Stock Service:      http://localhost:8082"
echo "  - Trading Service:    http://localhost:8083"
echo "  - Event Service:      http://localhost:8084"
echo "  - Scheduler Service:  http://localhost:8085"
echo "  - News Service:       http://localhost:8086"
echo ""
echo "Remote Infrastructure (210.121.177.150):"
echo "  - PostgreSQL:      5432"
echo "  - Redis:           6380"
echo "  - MongoDB:         27018"
echo "  - Kafka:           9093"
echo ""
echo "Logs are saved in ./logs/"
echo ""
echo "To stop all services, run: ./stop-local-services.sh"
