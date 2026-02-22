# Stock Simulator

> AI 기반 이벤트 주도형 모의 주식 거래 게임

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![SvelteKit](https://img.shields.io/badge/SvelteKit-2.49-FF3E00?logo=svelte)](https://kit.svelte.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Note**: 프론트엔드(SvelteKit) 및 인프라 구축(Docker, Prometheus, Grafana)은 **Claude AI**를 활용하여 개발되었습니다.

## 프로젝트 개요

Stock Simulator는 **AI 기반의 동적 시장 생태계**를 제공하는 모의 주식 거래 게임입니다. 실시간 IPO/상장폐지, NPC 투자자 생성, 체결 기반 주가 변동, AI 뉴스 이벤트가 주가에 영향을 주는 생동감 있는 주식 시장을 경험할 수 있습니다.

### 핵심 기능

- **동적 기업 생태계**: 30분마다 IPO(40% 확률), 1시간마다 상장폐지(10% 확률)
- **AI 투자자**: 개인 투자자(10분마다 1~3명), 기관 투자자(2시간마다 50% 확률) 자동 생성
- **체결 기반 주가 변동**: 주문 체결 → 시가총액/변동성 기반 가격 영향도 계산 → 실시간 주가 반영
- **실시간 호가창**: Redis 기반 주문 매칭 엔진 및 호가창
- **시간 가속**: 현실 1시간 = 게임 4시간 (하루 8사이클)
- **연속 게임**: 시즌 리셋 없이 지속적으로 진화하는 시장

### 게임 메커니즘

| 항목 | 설명 |
|------|------|
| **시간 비율** | 1:4 (현실 1시간 = 게임 4시간) |
| **거래 시간** | 게임 09:00~21:00 (실제 3시간) |
| **초기 자본** | 5,000,000원 |
| **총 종목 수** | 동적 (초기 ~500개, IPO/상장폐지로 변동) |
| **산업 분야** | IT, 농업, 제조, 서비스, 부동산, 명품, 식품 |
| **시가총액** | SMALL(~100억), MID(~1000억), LARGE(~1조) |

## 기술 스택

### Frontend

```
SvelteKit 2.49  +  Svelte 5  +  TypeScript 5.9  +  TailwindCSS 4.1  +  Vite 7.2
```

- **패키지 매니저**: pnpm
- **UI**: Lucide Icons
- **모니터링**: prom-client (Prometheus 메트릭)

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
  - kotlinx.serialization 1.9.0
  - Redisson 3.40.2 (Redis 클라이언트)

### Infrastructure

```
Docker Compose  +  Kafka (KRaft)  +  PostgreSQL 16  +  MongoDB 7.0  +  Redis 7
```

| 서비스 | 컨테이너 | 외부 포트 | 용도 |
|--------|----------|-----------|------|
| PostgreSQL Primary | stockSimulator-postgres | 5432 | 관계형 데이터 (Write) |
| PostgreSQL Replica | stockSimulator-postgres-replica | 5433 | 읽기 전용 복제본 |
| MongoDB | stockSimulator-mongo | 27018 | 로그, 뉴스, 이벤트 |
| Redis | stockSimulator-redis | 6380 | 캐시, 호가창, 세션, 랭킹 |
| Kafka (KRaft) | stockSimulator-kafka | 9094 | 이벤트 스트리밍 |
| Kafka UI | stockSimulator-kafka-ui | 8089 | Kafka 모니터링 |
| Elasticsearch | stockSimulator-elasticsearch | 9201 | 검색 엔진 |
| Prometheus | stockSimulator-prometheus | 9091 | 메트릭 수집 |
| Grafana | stockSimulator-grafana | 3001 | 대시보드 |
| Loki | stockSimulator-loki | 3100 | 로그 집계 |
| Eureka | stockSimulator-eureka-server | 8761 | 서비스 디스커버리 |
| API Gateway | stockSimulator-api-gateway | 9832 | API 라우팅 |
| Frontend | stockSimulator-frontend | 8080 | SvelteKit 앱 |
| TeamCity Server | stockSimulator-teamcity-server | 8111 | CI/CD |

## 프로젝트 구조

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
│   │       │   ├── dashboard/
│   │       │   ├── investors/
│   │       │   ├── mypage/
│   │       │   ├── news/
│   │       │   ├── ranking/
│   │       │   └── stocks/[stockId]/
│   │       ├── m/              # 모바일 라우트 (동일 구조)
│   │       ├── login/
│   │       └── signup/
│   └── package.json
│
├── backend/                     # Kotlin/Spring Boot MSA
│   ├── common/                 # 공유 모듈 (DTO, 이벤트, 예외, PriceUtil)
│   ├── eureka-server/          # 서비스 디스커버리 (8761)
│   ├── api-gateway/            # API 게이트웨이 (9832→8080)
│   ├── user-service/           # 인증, 사용자 관리 (8081)
│   ├── stock-service/          # 주식, 가격 관리 (8082)
│   ├── trading-service/        # 주문 매칭, 호가창 (8083)
│   ├── event-service/          # 게임 이벤트 (8084) [미구현]
│   ├── scheduler-service/      # IPO/상장폐지, 투자자 생성 (8085)
│   └── news-service/           # AI 뉴스 (8086) [미구현]
│
├── infra/                       # 인프라 설정
│   ├── grafana/provisioning/   # Grafana 대시보드 (5개)
│   ├── prometheus/             # Prometheus 설정
│   ├── loki/                   # Loki + Promtail 설정
│   ├── postgres/               # DB 초기화 스크립트
│   └── nginx/                  # Nginx 리버스 프록시 (dev)
│
├── doc/                         # 한글 설계 문서
│   ├── 모의주식게임_기획서_v1.0.md
│   ├── 모의주식게임_개발로드맵.md
│   ├── 트레이딩엔진_설계서.md
│   └── SVELTEKIT_DEVELOPMENT_TEMPLATE.md
│
├── .teamcity/                   # TeamCity Kotlin DSL 파이프라인
├── teamcity/                    # TeamCity 인프라 (Server + Agent)
├── docker-compose.yml
└── .env.example
```

### Backend 서비스 아키텍처 (Hexagonal)

```
{service}/
└── src/main/kotlin/com/stocksimulator/{service}/
    ├── domain/                # 엔티티, 도메인 로직
    ├── application/           # 유스케이스, 포트, 핸들러
    │   ├── dto/command/      # 커맨드 DTO
    │   ├── dto/result/       # 결과 DTO
    │   ├── handler/          # 커맨드/쿼리 핸들러
    │   └── port/
    │       ├── in/           # 인바운드 포트 (유스케이스)
    │       └── out/          # 아웃바운드 포트 (영속성, 이벤트)
    └── infrastructure/
        ├── config/           # Security, Redis, Swagger 설정
        └── adapter/
            ├── in/
            │   ├── web/      # REST 컨트롤러
            │   └── event/    # Kafka 컨슈머
            └── out/
                ├── persistence/  # JPA 리포지토리
                ├── event/        # Kafka 프로듀서
                └── client/       # Feign 클라이언트
```

## 시작하기

### 사전 요구사항

- **Docker** & **Docker Compose**
- **Java 21+** (백엔드 개발 시)
- **Node.js 20+** & **pnpm** (프론트엔드 개발 시)

### 1. 환경 변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 실제 환경에 맞게 수정
```

### 2. Docker Compose 실행

```bash
# 전체 서비스 시작
docker-compose --profile all up -d

# 프로필별 선택 실행
docker-compose --profile infra up -d         # 인프라만 (DB, Redis, Kafka 등)
docker-compose --profile services up -d      # 마이크로서비스만
docker-compose --profile monitoring up -d    # 모니터링만 (Prometheus, Grafana, Loki)
docker-compose --profile frontend up -d      # 프론트엔드만

# 빌드와 함께 시작
docker-compose --profile all up -d --build

# 상태 확인
docker-compose --profile all ps

# 로그 확인
docker logs stockSimulator-<service-name> 2>&1 | tail -50
```

### 3. 서비스 확인

| 서비스 | URL |
|--------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:9832 |
| Kafka UI | http://localhost:8089 |
| Grafana | http://localhost:3001 |
| Prometheus | http://localhost:9091 |
| TeamCity | http://localhost:8111 |

## 개발 가이드

### Frontend

```bash
cd frontend
pnpm install          # 의존성 설치
pnpm run dev          # 개발 서버 (http://localhost:5173)
pnpm run build        # 프로덕션 빌드
pnpm run check        # 타입 체크
pnpm run format       # 코드 포맷팅
pnpm run lint         # 린트
```

**규칙:**
- 모든 스타일은 `src/styles/` 내 별도 CSS 파일로 관리 (컴포넌트 내 `<style>` 태그 금지)
- CSS 변수로 다크/라이트 모드 지원
- 모바일 라우트: `/m/페이지명`, 데스크톱: `/페이지명`
- API 통신: `$lib/api/api.ts` 사용, `ApiResponse<T>` 구조

### Backend

```bash
# 전체 빌드
./gradlew build -x test

# 특정 서비스 빌드
./gradlew :backend:stock-service:build -x test

# 클린 빌드
./gradlew clean build -x test

# 테스트 실행
./gradlew test
```

**서비스별 설정:**
- `application.yml`: 로컬 개발 (localhost)
- `application-docker.yml`: Docker 환경 (환경 변수)

## 이벤트 기반 시장 동역학

### Kafka 이벤트 토픽

| 토픽 | 발행자 | 구독자 | 설명 |
|------|--------|--------|------|
| `stock.listed` | Scheduler | Stock Service | IPO (신규 상장) |
| `stock.delisted` | Scheduler | Stock Service | 상장폐지 |
| `order.matched` | Trading Service | Stock Service | 주문 체결 → 주가 변동 |
| `order.created` | Trading Service | - | 주문 생성 |
| `order.cancelled` | Trading Service | - | 주문 취소 |
| `price.updated` | Stock Service | Frontend, NPC 등 | 주가 변동 |
| `orderbook.updated` | Trading Service | Frontend | 호가창 변경 |
| `investor.created` | Scheduler | - | NPC/기관 투자자 생성 |
| `npc.created` | Scheduler | Stock Service | 개인 NPC 생성 |
| `institution.created` | Scheduler | Stock Service | 기관 투자자 생성 |
| `schedule.trade` | Scheduler | Trading Service | NPC/기관 매매 스케줄 |
| `event.occurred` | Event Service | Stock Service | 게임 이벤트 발생 |
| `news.published` | News Service | Frontend | AI 뉴스 발행 |

### 주가 변동 파이프라인

```
Trading Service                    Stock Service
  │  ORDER_MATCHED ──Kafka──→  OrderMatchedEventConsumer (수신)
                                   │
                               StockPriceCommandHandler (주가 계산)
                                   ├─ PriceUtil.calculatePriceImpact()
                                   │    (체결금액 / 시가총액 × 변동성 × 방향)
                                   ├─ PriceUtil.applyPriceChange()
                                   ├─ getPriceLimits() (상한/하한 30%)
                                   ├─ StockModel.updatePrice()
                                   ├─ DB 저장
                                   │
                               KafkaStockEventPublisher
                                   │  PRICE_UPDATED ──→ Frontend / NPC 등
```

### IPO & 상장폐지 스케줄

```
Scheduler Service (매 30분)
  ├─ 40% 확률로 IPO 실행
  │  ├─ 랜덤 기업명, 섹터, 초기가, 재무지표 생성
  │  └─ Kafka: stock.listed → Stock Service 종목 등록
  └─

Scheduler Service (매 1시간)
  ├─ 10% 확률로 상장폐지 조건 검사
  │  ├─ 조건: 낮은 시가총액/거래량, 액면가 이하
  │  └─ Kafka: stock.delisted → Stock Service 상태 변경
  └─
```

### 투자자 생성 스케줄

```
개인 투자자 (매 10분)
  ├─ 1~3명 생성
  ├─ 자본금: 20만 ~ 1억원
  ├─ 주간 수입: 자본금의 5%
  └─ 스타일: AGGRESSIVE / STABLE / VALUE / RANDOM

기관 투자자 (매 2시간, 50% 확률)
  ├─ 1개 기관 생성
  ├─ 자본금: 10억 ~ 1조원
  ├─ 일간 수입: 자본금의 1%
  └─ 스타일: AGGRESSIVE / STABLE / VALUE
```

## 모니터링

### Grafana 대시보드

**URL**: http://localhost:3001

5개 대시보드 제공 (자동 프로비저닝):
- **Stock Simulator - Services Overview**: 서비스 상태, 요청률, 응답 시간(p95), JVM 메모리, CPU
- **Backend Dashboard**: 백엔드 서비스 상세 메트릭
- **Frontend Dashboard**: 프론트엔드 성능 메트릭
- **Infrastructure Dashboard**: DB, Redis, Kafka, ES 인프라 메트릭
- **Logs Overview**: Loki 기반 실시간 로그 조회

### Prometheus Exporters

모든 Spring Boot 서비스 `/actuator/prometheus` + 전용 Exporter:
- Node Exporter (호스트 메트릭)
- cAdvisor (컨테이너 메트릭)
- PostgreSQL Exporter
- Redis Exporter
- MongoDB Exporter
- Kafka Exporter
- Elasticsearch Exporter

### Loki (로그 집계)

```logql
# 특정 서비스 로그
{container_name="stockSimulator-stock-service"}

# 에러 로그 필터
{container_name=~"stockSimulator-.*-service"} |= "ERROR"

# 서비스별 로그 비율
sum by (container_name) (rate({container_name=~"stockSimulator-.*"}[1m]))
```

## CI/CD

### TeamCity 파이프라인

**URL**: http://localhost:8111
**설정**: `.teamcity/settings.kts` (Kotlin DSL, Git 버전 관리)

**빌드 단계:**
1. Gradle `clean build -x test --parallel` (백엔드 전체)
2. Backend Docker 이미지 빌드 (8개 서비스)
3. Frontend Docker 이미지 빌드
4. 순차 배포 (Eureka → Services → Gateway → Frontend)
5. 헬스체크 (Eureka, API Gateway)

**트리거:**
- VCS trigger: master 브랜치 push 시 자동
- 수동 트리거: TeamCity UI

**TeamCity 설정:**
```bash
cd teamcity
chmod +x setup.sh
./setup.sh
```

### GitHub Container Registry (GHCR)

모든 Docker 이미지는 GHCR에서 호스팅:

```
ghcr.io/park-gijun/stocksim/eureka-server:latest
ghcr.io/park-gijun/stocksim/api-gateway:latest
ghcr.io/park-gijun/stocksim/user-service:latest
ghcr.io/park-gijun/stocksim/stock-service:latest
ghcr.io/park-gijun/stocksim/trading-service:latest
ghcr.io/park-gijun/stocksim/event-service:latest
ghcr.io/park-gijun/stocksim/scheduler-service:latest
ghcr.io/park-gijun/stocksim/news-service:latest
```

## 문제 해결

### Kafka 데이터 리셋 (KRaft 모드)

```bash
docker-compose --profile all down
docker volume rm stock-simulator_kafka_data
docker-compose --profile all up -d
```

### Eureka 서버 포트 오류

`docker-compose.yml`에 `SERVER_PORT=8761` 환경 변수 확인

### 서비스 Eureka 연결 실패

`application-docker.yml`에서 `eureka-server:8761` 사용 확인 (localhost 아님)

### 데이터베이스 초기화

```bash
# PostgreSQL 스키마 재생성
docker exec -it stockSimulator-postgres psql -U $POSTGRES_USER -d $POSTGRES_DB \
  -f /docker-entrypoint-initdb.d/init-schemas.sql

# Redis 캐시 클리어
docker exec -it stockSimulator-redis redis-cli -a $REDIS_PASSWORD FLUSHALL
```

## 로드맵

- [x] **Phase 1**: Docker 인프라 구축
  - [x] PostgreSQL (Primary + Replica Streaming Replication)
  - [x] MongoDB, Redis, Kafka (KRaft 모드)
  - [x] Prometheus, Grafana, Loki + Promtail
  - [x] 인프라 Exporter (Node, cAdvisor, PG, Redis, Mongo, Kafka, ES)
- [x] **Phase 2**: Backend MSA 구현
  - [x] Eureka, API Gateway (Spring Cloud Gateway 5.0.0, Java Config)
  - [x] 8개 마이크로서비스 (Hexagonal Architecture)
  - [x] GHCR (GitHub Container Registry) 이미지 배포
- [ ] **Phase 3**: 이벤트 시스템 & 거래 엔진
  - [x] IPO/상장폐지 스케줄러
  - [x] 투자자 생성 스케줄러 (개인 NPC + 기관)
  - [x] 주문 매칭 엔진 (OrderBook, Redis 캐시)
  - [x] 체결 기반 주가 변동 (order.matched → price.updated)
  - [ ] AI 뉴스 생성 (Event Service, News Service)
- [ ] **Phase 4**: Frontend 구현
  - [x] 인증 (로그인/회원가입)
  - [x] 투자자 관리 페이지
  - [x] 주식 목록 (API 연동)
  - [ ] 대시보드
  - [ ] 주식 상세 / 호가창
  - [ ] 포트폴리오 / 랭킹
- [ ] **Phase 5**: AI 통합
  - [ ] AI 기반 뉴스 생성
  - [ ] NPC 투자 전략 AI
- [x] **Phase 6**: CI/CD
  - [x] TeamCity Kotlin DSL 파이프라인
  - [x] Docker 기반 프로덕션 환경
  - [x] GHCR 이미지 자동 빌드/배포

## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

**코드 스타일:**
- Kotlin: [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- TypeScript: Prettier (tabs, single quotes, 100 chars)
- Svelte 5 Runes 문법 사용

## 주요 문서

| 문서 | 설명 |
|------|------|
| `doc/모의주식게임_기획서_v1.0.md` | 기능 명세서 |
| `doc/모의주식게임_개발로드맵.md` | 개발 로드맵 |
| `doc/트레이딩엔진_설계서.md` | 트레이딩 엔진 설계 |
| `doc/SVELTEKIT_DEVELOPMENT_TEMPLATE.md` | 프론트엔드 개발 가이드 |

## 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 제작자

**Stock Simulator Team**

- Backend Architecture & Development: Park Gijun
- Frontend & Infrastructure (AI-Assisted): Claude AI (Anthropic)

---

<div align="center">
Made with care by Stock Simulator Team
</div>
