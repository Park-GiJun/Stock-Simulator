@echo off
chcp 65001 > nul
echo =========================================
echo OpenAPI 설정 업데이트 - 서비스 재배포
echo =========================================

REM 1. 모든 서비스 중지
echo.
echo [1/4] 기존 서비스 중지...
docker-compose --profile all down

REM 2. 백엔드 빌드 (테스트 제외)
echo.
echo [2/4] 백엔드 서비스 빌드...
call gradlew.bat clean build -x test

REM 3. Docker 이미지 재빌드 및 서비스 시작
echo.
echo [3/4] Docker 이미지 재빌드 및 서비스 시작...
docker-compose --profile all up -d --build

REM 4. 서비스 상태 확인
echo.
echo [4/4] 서비스 상태 확인 (30초 대기 후)...
timeout /t 30 /nobreak > nul

docker-compose --profile all ps

echo.
echo =========================================
echo 재배포 완료!
echo =========================================
echo.
echo 확인 URL:
echo - API Gateway Swagger UI: http://localhost:9832/swagger-ui.html
echo - API Gateway OpenAPI JSON: http://localhost:9832/v3/api-docs
echo.
echo 개별 서비스 Swagger UI (Gateway 없이 직접 접근):
echo - User Service: http://localhost:8081/swagger-ui.html
echo - Stock Service: http://localhost:8082/swagger-ui.html
echo - Trading Service: http://localhost:8083/swagger-ui.html
echo - Event Service: http://localhost:8084/swagger-ui.html
echo - Scheduler Service: http://localhost:8085/swagger-ui.html
echo - News Service: http://localhost:8086/swagger-ui.html
echo.
echo Eureka Dashboard: http://localhost:8761
echo.
pause
