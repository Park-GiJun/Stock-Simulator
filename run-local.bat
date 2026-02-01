@echo off
REM =============================================================================
REM 로컬 개발 환경 실행 스크립트 (Windows)
REM =============================================================================

echo ==================================
echo Stock Simulator - Local Development
echo ==================================
echo.

REM .env.local을 .env로 복사
echo 1. Setting up environment variables...
copy /Y .env.local .env
echo [OK] Using remote infrastructure (172.30.1.79)
echo.

REM Gradle 빌드
echo 2. Building backend services...
call gradlew.bat clean build -x test
if errorlevel 1 (
    echo [ERROR] Build failed!
    exit /b 1
)
echo [OK] Build completed
echo.

REM Docker Compose로 백엔드 서비스만 시작
echo 3. Starting backend services...
docker-compose up -d eureka-server api-gateway user-service stock-service trading-service event-service scheduler-service news-service

if errorlevel 1 (
    echo [ERROR] Failed to start services!
    exit /b 1
)
echo [OK] Services started
echo.

echo 4. Waiting for services to be ready (60 seconds)...
timeout /t 60 /nobreak
echo.

echo 5. Checking service status...
docker-compose ps
echo.

echo ==================================
echo [OK] Local development environment ready!
echo ==================================
echo.
echo Services:
echo   - Eureka:      http://localhost:8761
echo   - API Gateway: http://localhost:9832
echo   - User:        http://localhost:8081
echo   - Stock:       http://localhost:8082
echo   - Trading:     http://localhost:8083
echo   - Event:       http://localhost:8084
echo   - Scheduler:   http://localhost:8085
echo   - News:        http://localhost:8086
echo.
echo Infrastructure (Remote):
echo   - PostgreSQL:      172.30.1.79:5432
echo   - Redis:           172.30.1.79:6380
echo   - MongoDB:         172.30.1.79:27018
echo   - Kafka:           172.30.1.79:9093
echo   - Elasticsearch:   172.30.1.79:9201
echo.
echo To view logs:
echo   docker-compose logs -f [service-name]
echo.
echo To stop:
echo   docker-compose down
