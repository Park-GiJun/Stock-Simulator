# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stock-Simulator (모의 주식 게임) is an AI-driven mock stock trading game with event-based price dynamics. The project uses a SvelteKit frontend with a planned Kotlin/Spring Boot microservices backend.

**Current Status:** MVP phase - frontend scaffolding complete, backend structure ready.

## Tech Stack

- **Frontend:** SvelteKit 2.x, Svelte 5, TypeScript, TailwindCSS 4.x, Vite
- **Backend:** Kotlin 1.9.25, Spring Boot 3.5.7, Spring Cloud 2024, WebFlux, JPA + Kotlin JDSL 3.5.4, Redisson
- **Package Manager:** pnpm (frontend), Gradle Kotlin DSL (backend)

## Common Commands

### Frontend (from `frontend/` directory)

```bash
# Install dependencies
pnpm install

# Development server
pnpm run dev

# Production build
pnpm run build

# Preview production build
pnpm run preview

# Type checking
pnpm run check
pnpm run check:watch

# Format code
pnpm run format

# Lint code
pnpm run lint
```

### Backend (from `backend/` directory)
```bash
# Build all modules
./gradlew build

# Run specific service
./gradlew :user-service:bootRun
./gradlew :stock-service:bootRun
./gradlew :eureka-server:bootRun
./gradlew :api-gateway:bootRun

# Clean build
./gradlew clean build

# Run tests
./gradlew test
```

## Architecture

### Current Structure

```
Stock-Simulator/
├── doc/                    # Korean-language design documents
├── frontend/               # SvelteKit application
├── backend/                # Kotlin/Spring Boot MSA
│   ├── common/            # Shared DTOs, exceptions, events
│   ├── eureka-server/     # Service discovery (port 8761)
│   ├── api-gateway/       # API Gateway (port 8080)
│   ├── user-service/      # Auth, users (port 8081)
│   ├── stock-service/     # Stocks, prices (port 8082)
│   ├── trading-service/   # Orders, portfolio (port 8083)
│   ├── event-service/     # Game events (port 8084)
│   ├── scheduler-service/ # NPC trading (port 8085)
│   ├── news-service/      # News articles (port 8086)
│   └── season-service/    # Rankings (port 8087)
├── infra/                  # Prometheus, Grafana configs
└── docker-compose.yml
```

### Backend Architecture

Each service follows **Hexagonal Architecture**:
```
service/
└── src/main/kotlin/com/stocksimulator/{service}/
    ├── domain/            # Entities, domain logic
    ├── application/       # Use cases, services
    └── adapter/
        ├── in/web/        # REST controllers
        └── out/persistence/ # JPA repositories
```

**Service Dependencies:**
- All services register with Eureka, routed through API Gateway
- MySQL: user, stock, trading, event, scheduler, season (with Flyway migrations)
- MongoDB: event (logs), news
- Redis/Redisson: stock (prices), trading (orderbook), season (ranking)
- Kafka: inter-service events

## Docker Infrastructure

Start all services:
```bash
docker-compose up -d
```

| Service | Container | External Port | Credentials |
|---------|-----------|---------------|-------------|
| MySQL | stockSimulator-mysql | 3307 | user: `stocksim`, pw: `stocksim123`, db: `stocksimulator` |
| MongoDB | stockSimulator-mongo | 27018 | user: `stocksim`, pw: `stocksim123` |
| Redis | stockSimulator-redis | 6380 | pw: `stocksim123` |
| Kafka | stockSimulator-kafka | 9093 | - |
| Kafka UI | stockSimulator-kafka-ui | 8089 | http://localhost:8089 |
| Elasticsearch | stockSimulator-elasticsearch | 9201 | - |
| Prometheus | stockSimulator-prometheus | 9091 | http://localhost:9091 |
| Grafana | stockSimulator-grafana | 3001 | http://localhost:3001 (admin/stocksim123) |

Configuration files:
- `infra/prometheus/prometheus.yml` - Prometheus scrape config
- `infra/grafana/provisioning/` - Grafana datasources and dashboards

## Code Style

- **Formatting:** Prettier with tabs, single quotes, 100 char line width
- **TypeScript:** Strict mode enabled
- **Svelte:** Uses Svelte 5 runes syntax

## Key Documentation

Design specifications are in Korean in the `doc/` directory:
- `모의주식게임_기획서_v1.0.md` - Comprehensive feature spec including time scaling, economic balance, stock parameters, event system, investor AI behaviors
- `모의주식게임_개발로드맵.md` - 6-phase development roadmap with milestones
- `SVELTEKIT_DEVELOPMENT_TEMPLATE.md` - **Must read** for frontend development guidelines

## Frontend Development Rules (from SVELTEKIT_DEVELOPMENT_TEMPLATE.md)

### Critical CSS Rules
- **All styles must be in separate CSS files** (`src/styles/`) - NO `<style>` tags in Svelte components
- Use CSS variables for all colors: `var(--color-primary)`, `var(--color-bg-primary)`, etc.
- Support dark/light mode via CSS variables

### CSS File Structure
```
src/styles/
├── global.css          # Global variables and utility classes
├── responsive.css      # Responsive utilities
├── components/         # Component styles (button.css, modal.css, etc.)
├── layouts/            # Layout styles (sidebar.css, header.css, etc.)
└── pages/              # Page-specific styles
```

### Mobile/Desktop Routing
- Desktop routes: `/페이지명` (e.g., `/dashboard`)
- Mobile routes: `/m/페이지명` (e.g., `/m/dashboard`)
- **Both desktop and mobile versions required** for every page

### Component Rules
- Use shared components from `$lib/components/` (Button, Input, Modal, Table, etc.)
- Icons: Use **Lucide** or **Heroicons** only - no custom SVG icons
- All component props must have TypeScript interfaces

### API Communication
- All responses follow `ApiResponse<T>` structure: `{ success, data, message, errorCode }`
- Use `$lib/api/api.ts` for all API calls
- Before backend: use mock data in `$lib/mock/` with `VITE_USE_MOCK=true`

### TypeScript Rules
- No `any` type - use proper interfaces
- All types in `$lib/types/` directory
- Component props must be typed via interfaces
