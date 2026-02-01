@echo off
REM =============================================================================
REM 로컬 개발 환경 실행 스크립트 (Windows)
REM =============================================================================
REM 서버 인프라(210.121.177.150) 사용, 백엔드 서비스만 로컬 실행

echo ==================================
echo Stock Simulator - Local Development
echo ==================================
echo.

REM 1. Eureka Server 실행
echo [1/8] Starting Eureka Server...
start "Eureka Server" gradlew.bat :backend:eureka-server:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8761 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin"

timeout /t 20 /nobreak > nul
echo [OK] Eureka Server started on http://localhost:8761
echo.

REM 2. API Gateway 실행
echo [2/8] Starting API Gateway...
start "API Gateway" gradlew.bat :backend:api-gateway:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8080 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin"

timeout /t 15 /nobreak > nul
echo [OK] API Gateway started on http://localhost:8080
echo.

REM 3. User Service 실행
echo [3/8] Starting User Service...
start "User Service" gradlew.bat :backend:user-service:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8081 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=users ^
    --spring.datasource.username=stocksim ^
    --spring.datasource.password=stocksim123 ^
    --spring.data.redis.host=210.121.177.150 ^
    --spring.data.redis.port=6380 ^
    --spring.data.redis.password=stocksim123 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin ^
    --spring.kafka.bootstrap-servers=210.121.177.150:9093"

timeout /t 10 /nobreak > nul
echo [OK] User Service started on http://localhost:8081
echo.

REM 4. Stock Service 실행
echo [4/8] Starting Stock Service...
start "Stock Service" gradlew.bat :backend:stock-service:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8082 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=stocks ^
    --spring.datasource.username=stocksim ^
    --spring.datasource.password=stocksim123 ^
    --spring.data.redis.host=210.121.177.150 ^
    --spring.data.redis.port=6380 ^
    --spring.data.redis.password=stocksim123 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin ^
    --spring.kafka.bootstrap-servers=210.121.177.150:9093"

timeout /t 10 /nobreak > nul
echo [OK] Stock Service started on http://localhost:8082
echo.

REM 5. Trading Service 실행
echo [5/8] Starting Trading Service...
start "Trading Service" gradlew.bat :backend:trading-service:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8083 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=trading ^
    --spring.datasource.username=stocksim ^
    --spring.datasource.password=stocksim123 ^
    --spring.data.redis.host=210.121.177.150 ^
    --spring.data.redis.port=6380 ^
    --spring.data.redis.password=stocksim123 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin ^
    --spring.kafka.bootstrap-servers=210.121.177.150:9093"

timeout /t 10 /nobreak > nul
echo [OK] Trading Service started on http://localhost:8083
echo.

REM 6. Event Service 실행
echo [6/8] Starting Event Service...
start "Event Service" gradlew.bat :backend:event-service:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8084 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=events ^
    --spring.datasource.username=stocksim ^
    --spring.datasource.password=stocksim123 ^
    --spring.data.redis.host=210.121.177.150 ^
    --spring.data.redis.port=6380 ^
    --spring.data.redis.password=stocksim123 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin ^
    --spring.kafka.bootstrap-servers=210.121.177.150:9093"

timeout /t 10 /nobreak > nul
echo [OK] Event Service started on http://localhost:8084
echo.

REM 7. Scheduler Service 실행
echo [7/8] Starting Scheduler Service...
start "Scheduler Service" gradlew.bat :backend:scheduler-service:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8085 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=scheduler ^
    --spring.datasource.username=stocksim ^
    --spring.datasource.password=stocksim123 ^
    --spring.data.redis.host=210.121.177.150 ^
    --spring.data.redis.port=6380 ^
    --spring.data.redis.password=stocksim123 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin ^
    --spring.kafka.bootstrap-servers=210.121.177.150:9093"

timeout /t 10 /nobreak > nul
echo [OK] Scheduler Service started on http://localhost:8085
echo.

REM 8. News Service 실행
echo [8/8] Starting News Service...
start "News Service" gradlew.bat :backend:news-service:bootRun ^
    --args="--spring.profiles.active=local ^
    --server.port=8086 ^
    --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ ^
    --spring.datasource.url=jdbc:postgresql://210.121.177.150:5432/stocksimulator?currentSchema=news ^
    --spring.datasource.username=stocksim ^
    --spring.datasource.password=stocksim123 ^
    --spring.data.redis.host=210.121.177.150 ^
    --spring.data.redis.port=6380 ^
    --spring.data.redis.password=stocksim123 ^
    --spring.data.mongodb.uri=mongodb://stocksim:stocksim123@210.121.177.150:27018/stocksimulator?authSource=admin ^
    --spring.kafka.bootstrap-servers=210.121.177.150:9093"

timeout /t 10 /nobreak > nul
echo [OK] News Service started on http://localhost:8086
echo.

echo ==================================
echo [OK] All services started!
echo ==================================
echo.
echo Local Services:
echo   - Eureka Server:      http://localhost:8761
echo   - API Gateway:        http://localhost:8080
echo   - User Service:       http://localhost:8081
echo   - Stock Service:      http://localhost:8082
echo   - Trading Service:    http://localhost:8083
echo   - Event Service:      http://localhost:8084
echo   - Scheduler Service:  http://localhost:8085
echo   - News Service:       http://localhost:8086
echo.
echo Remote Infrastructure (210.121.177.150):
echo   - PostgreSQL:      5432
echo   - Redis:           6380
echo   - MongoDB:         27018
echo   - Kafka:           9093
echo.
echo Press any key to continue...
pause > nul
