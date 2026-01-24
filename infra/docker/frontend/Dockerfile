# =============================================================================
# Frontend Dockerfile - Multi-stage Build
# SvelteKit Application
# =============================================================================

# Build Stage
FROM node:20-alpine AS builder

WORKDIR /app

# pnpm 설치
RUN corepack enable && corepack prepare pnpm@latest --activate

# 의존성 파일 복사
COPY frontend/package.json frontend/pnpm-lock.yaml ./

# 의존성 설치
RUN pnpm install --frozen-lockfile

# 소스 복사
COPY frontend .

# 환경 변수 설정 (빌드 시점)
ARG PUBLIC_API_URL=http://172.30.1.79/api
ARG PUBLIC_WS_URL=ws://172.30.1.79/ws

ENV PUBLIC_API_URL=$PUBLIC_API_URL
ENV PUBLIC_WS_URL=$PUBLIC_WS_URL

# 빌드
RUN pnpm run build

# =============================================================================
# Production Stage - Nginx
# =============================================================================
FROM nginx:alpine

# 설정 파일 복사
COPY infra/docker/frontend/nginx.conf /etc/nginx/nginx.conf

# 빌드 결과물 복사
COPY --from=builder /app/build /usr/share/nginx/html

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
