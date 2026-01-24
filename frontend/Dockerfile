# Build stage
FROM node:20-alpine AS builder
WORKDIR /app

# pnpm 설치
RUN corepack enable && corepack prepare pnpm@latest --activate

# 의존성 설치
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

# 소스 복사 및 빌드
COPY . .

ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL

RUN pnpm run build

# Runtime stage
FROM node:20-alpine
WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

# 프로덕션 의존성만 복사
COPY --from=builder /app/build ./build
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules

# 포트 설정
EXPOSE 3000

# 환경변수
ENV HOST=0.0.0.0
ENV PORT=3000
ENV ORIGIN=http://localhost:3000

# SvelteKit Node adapter 실행
CMD ["node", "build"]
