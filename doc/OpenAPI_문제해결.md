# OpenAPI 문서 비어있는 문제 해결

## 문제 원인

`http://api.gijun.net/v3/api-docs`에서 OpenAPI 문서가 비어있었던 이유:

1. **각 마이크로서비스의 OpenAPI 설정 누락**: 각 서비스의 `application.yml` 및 `application-docker.yml`에 SpringDoc 설정이 없었음
2. **API Gateway의 SwaggerConfig 비효율**: GroupedOpenApi 방식은 Gateway 자체의 API를 그룹화하는 것이지, 다른 서비스의 API 문서를 통합하는 것이 아님

## 해결 내용

### 1. 각 서비스에 SpringDoc 설정 추가

다음 서비스들의 `application.yml`과 `application-docker.yml`에 SpringDoc 설정 추가:

- ✅ user-service
- ✅ stock-service
- ✅ trading-service
- ✅ event-service
- ✅ news-service
- ✅ scheduler-service

**추가된 설정:**
```yaml
# SpringDoc / OpenAPI Configuration
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

### 2. API Gateway SwaggerConfig 수정

`backend/api-gateway/src/main/kotlin/com/stocksimulator/gateway/config/SwaggerConfig.kt` 수정:

**변경 전:**
- GroupedOpenApi를 사용하여 Gateway 자체의 API만 그룹화
- 다른 서비스의 API 문서를 취합하지 못함

**변경 후:**
- `SwaggerUiConfigProperties`를 사용하여 각 서비스의 OpenAPI 문서 URL을 명시적으로 등록
- API Gateway가 각 서비스의 `/v3/api-docs`를 프록시하여 통합 UI 제공

### 3. API Gateway application.yml 정리

`backend/api-gateway/src/main/resources/application.yml`의 springdoc.swagger-ui.urls 부분 제거:
- SwaggerConfig.kt에서 프로그래밍 방식으로 처리하므로 중복 설정 제거

## 재배포 방법

### Windows 사용자:
```cmd
redeploy-with-openapi.bat
```

### Linux/Mac 사용자:
```bash
chmod +x redeploy-with-openapi.sh
./redeploy-with-openapi.sh
```

### 수동 재배포:
```bash
# 1. 서비스 중지
docker-compose --profile all down

# 2. 빌드
./gradlew clean build -x test

# 3. 재시작
docker-compose --profile all up -d --build

# 4. 상태 확인
docker-compose --profile all ps
```

## 확인 방법

### 1. API Gateway를 통한 통합 문서 (권장)

**Swagger UI:**
```
http://localhost:9832/swagger-ui.html
```

**OpenAPI JSON:**
```
http://localhost:9832/v3/api-docs
```

**각 서비스별 문서 (Gateway를 통해):**
- User Service: http://localhost:9832/user-service/v3/api-docs
- Stock Service: http://localhost:9832/stock-service/v3/api-docs
- Trading Service: http://localhost:9832/trading-service/v3/api-docs
- Event Service: http://localhost:9832/event-service/v3/api-docs
- News Service: http://localhost:9832/news-service/v3/api-docs
- Scheduler Service: http://localhost:9832/scheduler-service/v3/api-docs

### 2. 개별 서비스 직접 접근 (디버깅용)

각 서비스에 직접 접근하여 OpenAPI 문서 확인:

- User Service: http://localhost:8081/swagger-ui.html
- Stock Service: http://localhost:8082/swagger-ui.html
- Trading Service: http://localhost:8083/swagger-ui.html
- Event Service: http://localhost:8084/swagger-ui.html
- Scheduler Service: http://localhost:8085/swagger-ui.html
- News Service: http://localhost:8086/swagger-ui.html

## 기대 결과

### Swagger UI에서:

1. **드롭다운 메뉴**가 표시되어 각 서비스를 선택할 수 있음
   - User Service
   - Stock Service
   - Trading Service
   - Event Service
   - News Service
   - Scheduler Service

2. **각 서비스의 API 엔드포인트**가 정상적으로 표시됨
   - User Service 예시:
     - POST /api/v1/users/signup
     - POST /api/v1/users/login
     - POST /api/v1/users/logout
     - GET /api/v1/users/me

3. **Try it out** 기능으로 실제 API 테스트 가능

### OpenAPI JSON에서:

각 서비스의 `/v3/api-docs`에서 다음과 같은 구조가 반환됨:

```json
{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [...],
  "paths": {
    "/api/v1/users/signup": { ... },
    "/api/v1/users/login": { ... },
    ...
  },
  "components": {
    "schemas": { ... }
  }
}
```

## 트러블슈팅

### 1. 여전히 빈 문서가 나오는 경우

**원인:** 서비스가 재시작되지 않았거나 빌드가 제대로 되지 않음

**해결:**
```bash
# 완전히 정리하고 다시 시작
docker-compose --profile all down
docker system prune -f
./gradlew clean build -x test
docker-compose --profile all up -d --build
```

### 2. 특정 서비스만 문서가 안 나오는 경우

**원인:** 해당 서비스에 컨트롤러가 없거나 제대로 등록되지 않음

**해결:**
```bash
# 해당 서비스 로그 확인
docker logs stockSimulator-user-service 2>&1 | grep -i "springdoc\|openapi"

# 서비스 재시작
docker-compose --profile all restart user-service
```

### 3. Gateway를 통한 접근이 안 되는 경우

**원인:** Gateway 라우팅 설정 문제

**해결:**
```bash
# Gateway 로그 확인
docker logs stockSimulator-api-gateway 2>&1 | tail -50

# Gateway 라우팅 확인
curl http://localhost:9832/actuator/gateway/routes
```

### 4. CORS 에러가 발생하는 경우

**원인:** API Gateway의 CORS 설정 누락

**해결:** `backend/api-gateway/src/main/kotlin/com/stocksimulator/gateway/config/CorsConfig.kt` 확인 및 추가 필요

## 참고 사항

- **SpringDoc 버전**: 2.8.4 (최신 버전)
- **Spring Cloud Gateway**: WebFlux 기반
- **Gateway 포트 변경**: 8080 → 9832 (application.yml에서 확인 필요)
- **각 서비스의 OpenAPI는 독립적**: Gateway는 단순히 프록시 역할

## 다음 단계

1. ✅ 재배포 후 Swagger UI 확인
2. ✅ 각 서비스별 API 문서 테스트
3. ⏭️ API 문서에 설명(description) 추가
4. ⏭️ 요청/응답 스키마 상세화
5. ⏭️ 예시(examples) 추가
6. ⏭️ 인증/권한 정보 추가 (@SecurityRequirement)

## 문서 작성일

2025-02-01
