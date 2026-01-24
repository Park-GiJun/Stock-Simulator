# =============================================================================
# Backend Dockerfile - Multi-stage Build
# 모든 Spring Boot 서비스 공용
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 공통 모듈 복사
COPY common common

# 서비스별 빌드를 위한 ARG
ARG SERVICE_NAME

# 서비스 소스 복사
COPY ${SERVICE_NAME} ${SERVICE_NAME}

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 캐싱을 위해 먼저 dependencies만 다운로드
RUN ./gradlew :${SERVICE_NAME}:dependencies --no-daemon || true

# 빌드 실행
RUN ./gradlew :${SERVICE_NAME}:bootJar --no-daemon -x test

# =============================================================================
# Runtime Stage
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

ARG SERVICE_NAME

# JAR 파일 복사
COPY --from=builder /app/${SERVICE_NAME}/build/libs/*.jar app.jar

# 설정 파일용 디렉토리
RUN mkdir -p /app/config && chown -R appuser:appgroup /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# JVM 옵션
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
