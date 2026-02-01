#!/bin/bash

echo "=== API Gateway 수정 및 재배포 스크립트 ==="
echo ""

echo "1. API Gateway 빌드 중..."
./gradlew :backend:api-gateway:build -x test

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패!"
    exit 1
fi

echo "✅ 빌드 완료"
echo ""

echo "2. API Gateway 컨테이너 재시작 중..."
docker-compose --profile all up -d --build api-gateway

if [ $? -ne 0 ]; then
    echo "❌ 컨테이너 재시작 실패!"
    exit 1
fi

echo "✅ 컨테이너 재시작 완료"
echo ""

echo "3. 헬스 체크 대기 중 (20초)..."
sleep 20

echo ""
echo "4. API Gateway 로그 확인:"
docker logs stockSimulator-api-gateway --tail 30

echo ""
echo "=== 재배포 완료 ==="
echo ""
echo "테스트 명령어:"
echo "  curl http://localhost:9832/user-service/actuator/health"
echo "  curl http://localhost:9832/user-service/api/v1/users/signup"
