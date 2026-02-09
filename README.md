# 📈 Stock Simulator

> AI 기반 이벤트 주도형 모의 주식 거래 게임

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![SvelteKit](https://img.shields.io/badge/SvelteKit-2.49-FF3E00?logo=svelte)](https://kit.svelte.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Note**: 프론트엔드(SvelteKit) 및 인프라 구축(Docker, Prometheus, Grafana)은 **Claude AI**를 활용하여 개발되었습니다.

## 🎯 프로젝트 개요

Stock Simulator는 **AI 기반의 동적 시장 생태계**를 제공하는 모의 주식 거래 게임입니다. 실시간 IPO/상장폐지, NPC 투자자 생성, AI 뉴스 이벤트가 주가에 영향을 주는 생동감 있는 주식 시장을 경험할 수 있습니다.

### ✨ 핵심 기능

- 🏢 **동적 기업 생태계**: 30분마다 IPO, 1시간마다 상장폐지 (조건 기반)
- 🤖 **AI 투자자**: 개인 투자자(10분), 기관 투자자(2시간) 자동 생성
- 📰 **AI 뉴스 시스템**: 사회/산업/기업 레벨 이벤트로 주가 변동
- 📊 **실시간 호가창**: Redis 기반 실시간 주문 체결
- ⏱️ **시간 가속**: 현실 1시간 = 게임 4시간 (하루 3시간에 24시간 게임 진행)
- 🔄 **연속 게임**: 시즌 리셋 없이 지속적으로 진화하는 시장

### 🎮 게임 메커니즘

| 항목 | 설명 |
|------|------|
| **시간 비율** | 1:4 (현실 1시간 = 게임 4시간) |
| **거래 시간** | 게임 09:00~21:00 (실제 3시간) |
| **초기 자본** | 5,000,000원 |
| **총 종목 수** | 동적 (초기 ~500개, IPO/상장폐지로 변동) |
| **산업 분야** | IT, 농업, 제조, 서비스, 부동산, 명품, 식품 |
| **시가총액** | SMALL(~100억), MID(~1000억), LARGE(~1조) |

## 🏗️ 기술 스택

### Frontend
```
SvelteKit 2.49  +  Svelte 5  +  TypeScript  +  TailwindCSS 4.1
```
- **패키지 매니저**: pnpm
- **빌드 도구**: Vite 7.2
- **UI 라이브러리**: Lucide Icons
- **모니터링**: prom-client

### Backend
```
Kotlin 2.3.0  +  Spring Boot 4.0.1  +  Spring Cloud 2025.1.1
```
- **아키텍처**: Microservices (Hexagonal Architecture)
- **빌드 도구**: Gradle Kotlin DSL
- **주요 라이브러리**:
  - Spring WebFlux (비동기 처리)
  - Spring Data JPA + Kotlin JDSL 3.6.0
  - kotlinx.coroutines 1.10.2
  - Redisson 3.40.2 (Redis 클라이언트)

### Infrastructure
```
Docker Compose  +  Kafka  +  PostgreSQL  +  MongoDB  +  Redis
```

| 서비스 | 포트 | 용도 |
|--------|------|------|
| API Gateway | 9832 | API 라우팅 |
| Eureka | 8761 | 서비스 디스커버리 |
| PostgreSQL | 5432 | 관계형 데이터 (Primary) |
| PostgreSQL Replica | 5433 | 읽기 전용 복제본 |
| MongoDB | 27018 | 로그, 뉴스 |
| Redis | 6380 | 캐시, 호가창, 랭킹 |
| Kafka | 9093 | 이벤트 스트리밍 |
| Kafka UI | 8089 | Kafka 모니터링 |
| Elasticsearch | 9201 | 검색 엔진 |
| Prometheus | 9091 | 메트릭 수집 |
| Grafana | 3001 | 대시보드 |
| Jenkins | 8180 | CI/CD 파이프라인 |

## 📁 프로젝트 구조

```
Stock-Simulator/
├── frontend/                    # SvelteKit 애플리케이션
│   ├── src/
│   │   ├── lib/
│   │   │   ├── api/            # API 클라이언트
│   │   │   ├── components/     # 재사용 컴포넌트
│   │   │   └── styles/         # 글로벌 CSS (CSS 변수)
│   │   └── routes/
│   │       ├── (desktop)/      # 데스크톱 라우트
│   │       └── m/              # 모바일 라우트
│   └── package.json
│
├── backend/                     # Kotlin/Spring Boot MSA
│   ├── common/                 # 공유 모듈 (DTO, 이벤트, 예외)
│   ├── eureka-server/          # 서비스 디스커버리
│   ├── api-gateway/            # API 게이트웨이
│   ├── user-service/           # 인증, 사용자 관리
│   ├── stock-service/          # 주식, 가격 관리
│   ├── trading-service/        # 주문, 포트폴리오 관리
│   ├── event-service/          # 게임 이벤트 관리
│   ├── scheduler-service/      # IPO/상장폐지, 투자자 생성
│   └── news-service/           # AI 뉴스 생성
│
├── infra/                       # 인프라 설정
│   ├── grafana/provisioning/   # Grafana 대시보드
│   └── prometheus/             # Prometheus 설정
│
├── doc/                         # 한글 설계 문서
├── docker-compose.yml
├── .env.example
└── build.gradle.kts
```

### Backend 서비스 아키텍처 (Hexagonal)

```
{service}/
└── src/main/kotlin/com/stocksimulator/{service}/
    ├── domain/              # 엔티티, 도메인 로직
    ├── application/         # 유스케이스, 서비스
    └── adapter/
        ├── in/web/         # REST 컨트롤러
        └── out/persistence/ # JPA 리포지토리
```

## 🚀 시작하기

### 사전 요구사항

- **Docker** & **Docker Compose**
- **Java 21+** (백엔드 개발 시)
- **Node.js 20+** & **pnpm** (프론트엔드 개발 시)
- **Kotlin 2.3.0+** (백엔드 개발 시)

### 1. 환경 변수 설정

```bash
cp .env.example .env
```

`.env` 파일 수정:
```env
# 인프라 호스트 (환경에 맞게 수정)
EUREKA_HOST=your-server-ip
POSTGRES_HOST=your-server-ip
REDIS_HOST=your-server-ip
MONGO_HOST=your-server-ip
KAFKA_HOST=your-server-ip

# 데이터베이스 인증 (강력한 비밀번호로 변경 권장)
POSTGRES_USER=your-db-user
POSTGRES_PASSWORD=your-secure-password
MONGO_USER=your-mongo-user
MONGO_PASSWORD=your-secure-password
REDIS_PASSWORD=your-secure-password

# Spring 프로필
SPRING_PROFILES_ACTIVE=docker
```

> ⚠️ **보안 주의**: 프로덕션 환경에서는 반드시 강력한 비밀번호를 사용하고, `.env` 파일을 Git에 커밋하지 마세요.

### 2. Docker 컨테이너 실행

```bash
# 전체 서비스 시작
docker-compose --profile all up -d

# 빌드와 함께 시작
docker-compose --profile all up -d --build

# 상태 확인
docker-compose --profile all ps

# 로그 확인
docker logs stockSimulator-<service-name> 2>&1 | tail -50
```

**프로필별 실행:**
```bash
docker-compose --profile infra up -d       # 인프라만
docker-compose --profile services up -d    # 마이크로서비스만
docker-compose --profile monitoring up -d  # 모니터링만
```

### 3. 서비스 확인

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:9832
- **Grafana**: http://localhost:3001
- **Prometheus**: http://localhost:9091
- **Kafka UI**: http://localhost:8089
- **Jenkins**: http://localhost:8180

> 기본 Grafana 계정은 `.env` 파일에서 설정한 값을 사용합니다.

## 💻 개발 가이드

### Frontend 개발

```bash
cd frontend

# 의존성 설치
pnpm install

# 개발 서버 실행 (http://localhost:5173)
pnpm run dev

# 프로덕션 빌드
pnpm run build

# 타입 체크
pnpm run check

# 코드 포맷팅
pnpm run format

# 린트
pnpm run lint
```

**중요 규칙:**
- ✅ 모든 스타일은 `src/styles/` 내 별도 CSS 파일로 관리
- ❌ Svelte 컴포넌트 내 `<style>` 태그 사용 금지
- 📱 모바일 라우트: `/m/페이지명`, 데스크톱: `/페이지명`
- 🎨 CSS 변수로 다크/라이트 모드 지원

### Backend 개발

```bash
# 전체 빌드
./gradlew build -x test

# 특정 서비스 빌드
./gradlew :backend:user-service:build

# 클린 빌드
./gradlew clean build -x test

# 테스트 실행
./gradlew test

# 특정 서비스 실행 (로컬)
./gradlew :backend:user-service:bootRun
```

**서비스별 설정 파일:**
- `application.yml`: 로컬 개발 (localhost)
- `application-docker.yml`: Docker 환경 (환경 변수 사용)

## 🎪 이벤트 기반 시장 동역학

### Kafka 이벤트 토픽

| 토픽 | 발행자 | 구독자 | 설명 |
|------|--------|--------|------|
| `stock.listed` | Scheduler | Stock Service | IPO (신규 상장) |
| `stock.delisted` | Scheduler | Stock Service | 상장폐지 |
| `investor.created` | Scheduler | Trading Service | NPC/기관 투자자 생성 |
| `price.updated` | Stock Service | 전체 | 주가 변동 |
| `orderbook.updated` | Trading Service | 전체 | 호가창 변경 |
| `event.occurred` | Event Service | Stock Service | 게임 이벤트 발생 |
| `news.published` | News Service | 전체 | AI 뉴스 발행 |

### IPO & 상장폐지 스케줄

```
┌─────────────────────────────────────────┐
│  Scheduler Service (매 30분)            │
│  ├─ 30% 확률로 IPO 실행                 │
│  │  ├─ 랜덤 기업명, 섹터, 초기가 생성   │
│  │  └─ Kafka: stock.listed             │
│  └─ Stock Service가 종목 등록           │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Scheduler Service (매 1시간)           │
│  ├─ 10% 확률로 상장폐지 조건 검사       │
│  │  ├─ 조건: 낮은 시가총액/거래량      │
│  │  └─ Kafka: stock.delisted           │
│  └─ Stock Service가 종목 상태 변경      │
└─────────────────────────────────────────┘
```

### 투자자 생성 스케줄

```
┌─────────────────────────────────────────┐
│  개인 투자자 (매 10분)                   │
│  ├─ 1~3명 생성                          │
│  ├─ 자본금: 20만 ~ 1억원                │
│  ├─ 주간 수입: 자본금의 5%              │
│  └─ 스타일: AGGRESSIVE/STABLE/VALUE/RANDOM │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  기관 투자자 (매 2시간, 50% 확률)       │
│  ├─ 1개 기관 생성                       │
│  ├─ 자본금: 10억 ~ 1조원                │
│  ├─ 일간 수입: 자본금의 1%              │
│  └─ 스타일: AGGRESSIVE/STABLE/VALUE     │
└─────────────────────────────────────────┘
```

## 📊 모니터링

### Grafana 대시보드

**URL**: http://localhost:3001

**"Stock Simulator - Services Overview" 대시보드 포함:**
- ✅ 서비스 상태 (UP/DOWN)
- 📈 서비스별 요청률
- ⏱️ 응답 시간 (p95)
- 💾 JVM 메모리 사용량
- 🖥️ CPU 사용률
- 🧵 스레드 수
- 🔌 DB 커넥션 풀 상태

### Prometheus Metrics

모든 서비스는 `/actuator/prometheus` 엔드포인트로 메트릭 노출  
**타겟 확인**: http://localhost:9091/targets

## 🚢 CI/CD

### Jenkins 파이프라인

**URL**: http://localhost:8180

**주요 기능:**
- 🔄 Multi-stage 파이프라인 (Clean → Build → Deploy)
- 🎯 선택적 빌드 옵션 (All, Backend only, Frontend only)
- 🐳 Docker 이미지 빌드 및 GHCR 푸시
- ✅ 자동 배포 및 헬스체크
- 📢 Slack 알림 (선택적)

**빌드 트리거:**
- GitHub webhook (master 브랜치 push 시)
- Jenkins UI에서 수동 트리거

**배포 플로우:**
1. 작업 공간 정리 및 이전 Docker 이미지 삭제
2. Gradle로 백엔드 서비스 빌드 (Java 25)
3. pnpm으로 프론트엔드 빌드
4. Docker 이미지 빌드 및 GHCR 푸시
5. docker-compose로 배포
6. 헬스체크 (Eureka, API Gateway)

### GitHub Container Registry (GHCR)

모든 Docker 이미지는 GHCR에서 호스팅됩니다:

```
ghcr.io/park-gijun/stock-simulator-eureka-server:latest
ghcr.io/park-gijun/stock-simulator-api-gateway:latest
ghcr.io/park-gijun/stock-simulator-user-service:latest
ghcr.io/park-gijun/stock-simulator-stock-service:latest
ghcr.io/park-gijun/stock-simulator-trading-service:latest
ghcr.io/park-gijun/stock-simulator-event-service:latest
ghcr.io/park-gijun/stock-simulator-scheduler-service:latest
ghcr.io/park-gijun/stock-simulator-news-service:latest
ghcr.io/park-gijun/stock-simulator-frontend:latest
```

**이미지 Pull:**
```bash
docker login ghcr.io -u <github-username>
docker pull ghcr.io/park-gijun/stock-simulator-frontend:latest
```

## 🔧 문제 해결

### 1. Kafka Cluster ID Mismatch

**증상**: `InconsistentClusterIdException` 에러

**해결**:
```bash
docker-compose --profile all down
docker volume rm stock-simulator_kafka_data stock-simulator_zookeeper_data
docker-compose --profile all up -d
```

### 2. Eureka 서버 포트 오류

**증상**: Eureka가 8080 포트에서 시작됨

**해결**: `docker-compose.yml`에 `SERVER_PORT` 환경 변수 확인

### 3. 서비스 Eureka 연결 실패

**증상**: `Connection refused: localhost:8761`

**해결**: `application-docker.yml`에서 `eureka-server:8761` 사용 확인

### 4. 로그인 후 페이지 전환 오류 (해결됨)

**증상**: 로그인 성공했으나 홈페이지로 이동하지 않음

**원인**: `+layout.svelte`에서 로그인 직후 `getCurrentUser()` 호출로 인한 세션 타이밍 이슈

**해결**: `authStore`에 사용자 정보가 이미 있으면 세션 검증 스킵하도록 수정

### 5. 데이터베이스 초기화

```bash
# PostgreSQL 스키마 재생성
docker exec -it stockSimulator-postgres psql -U <your-db-user> -d <your-db-name> -f /docker-entrypoint-initdb.d/init-schemas.sql

# MongoDB 데이터 삭제
docker exec -it stockSimulator-mongo mongosh -u <your-mongo-user> -p <your-mongo-password> --eval "use stocksim; db.dropDatabase();"

# Redis 캐시 클리어
docker exec -it stockSimulator-redis redis-cli -a <your-redis-password> FLUSHALL
```

> 위 명령어에서 `<your-db-user>`, `<your-mongo-user>` 등은 `.env` 파일에 설정한 값으로 대체하세요.

## 📚 주요 문서

`doc/` 디렉토리의 설계 문서:
- `모의주식게임_기획서_v1.0.md` - 기능 명세서
- `모의주식게임_개발로드맵.md` - 개발 로드맵
- `인프라_구축_진행상황.md` - 인프라 구축 진행상황
- `SVELTEKIT_DEVELOPMENT_TEMPLATE.md` - 프론트엔드 개발 가이드

## 🗺️ 로드맵

- [x] **Phase 1**: Docker 인프라 구축
  - [x] PostgreSQL (Primary + Replica)
  - [x] MongoDB, Redis, Kafka
  - [x] Prometheus, Grafana, Loki
  - [x] Jenkins CI/CD 파이프라인
- [x] **Phase 2**: Backend MSA 구현
  - [x] Eureka, API Gateway (Spring Cloud Gateway 5.0.0)
  - [x] 7개 마이크로서비스
  - [x] GHCR (GitHub Container Registry) 마이그레이션
- [ ] **Phase 3**: 이벤트 시스템
  - [x] IPO/상장폐지 스케줄러
  - [x] 투자자 생성 스케줄러
  - [ ] AI 뉴스 생성
- [ ] **Phase 4**: Frontend 구현
  - [x] 인증 (로그인/회원가입)
  - [ ] 대시보드
  - [ ] 주식 목록/상세
  - [ ] 호가창/거래
  - [ ] 포트폴리오/랭킹
- [ ] **Phase 5**: AI 통합
  - [ ] OpenAI GPT 기반 뉴스 생성
  - [ ] NPC 투자 전략 AI
- [x] **Phase 6**: 배포
  - [x] Jenkins CI/CD 파이프라인
  - [x] Docker 기반 프로덕션 환경

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

**코드 스타일:**
- Kotlin: [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- TypeScript: Prettier (tabs, single quotes, 100 chars)
- Svelte 5 Runes 문법 사용

## 📝 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 제작자

**Stock Simulator Team**

- Backend Architecture & Development: Park Gijun
- Frontend & Infrastructure (AI-Assisted): Claude AI (Anthropic)

## 🤖 AI 활용

이 프로젝트는 다음 부분에서 **Claude AI**를 활용하여 개발되었습니다:

- **Frontend 전체**: SvelteKit 기반 UI/UX 구현
- **Infrastructure 구성**: Docker Compose, Prometheus, Grafana 설정
- **모니터링 시스템**: 메트릭 수집 및 대시보드 구성
- **코드 리뷰 및 최적화**: 성능 개선 및 베스트 프랙티스 적용

## 🙏 감사의 글

- [Spring Boot](https://spring.io/projects/spring-boot)
- [SvelteKit](https://kit.svelte.dev/)
- [Kotlin](https://kotlinlang.org/)
- [Docker](https://www.docker.com/)
- [Claude AI](https://www.anthropic.com/claude) - Frontend & Infrastructure Development

---

<div align="center">
Made with ❤️ by Stock Simulator Team<br>
Powered by Claude AI (Frontend & Infrastructure)
</div>
