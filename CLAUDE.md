# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stock-Simulator (모의 주식 게임) is an AI-driven mock stock trading game with event-based price dynamics. The project uses a SvelteKit frontend with a Kotlin/Spring Boot microservices backend.

**Current Status:** MVP phase - Docker infrastructure deployed, backend services running.

## Tech Stack

- **Frontend:** SvelteKit 2.x, Svelte 5, TypeScript, TailwindCSS 4.x, Vite
- **Backend:** Kotlin 1.9.25, Spring Boot 3.5.7, Spring Cloud 2024, WebFlux, JPA + Kotlin JDSL 3.5.4, Redisson
- **Package Manager:** pnpm (frontend), Gradle Kotlin DSL (backend)
- **Infrastructure:** Docker Compose, Prometheus, Grafana

## Common Commands

### Frontend (from `frontend/` directory)

```bash
pnpm install          # Install dependencies
pnpm run dev          # Development server
pnpm run build        # Production build
pnpm run preview      # Preview production build
pnpm run check        # Type checking
pnpm run format       # Format code
pnpm run lint         # Lint code
```

### Backend (from project root)
```bash
# Build all modules
./gradlew build -x test

# Build specific service
./gradlew :backend:eureka-server:build
./gradlew :backend:user-service:build
./gradlew :backend:api-gateway:build

# Clean build
./gradlew clean build -x test

# Run tests
./gradlew test
```

### Docker Compose
```bash
# Start all services
docker-compose --profile all up -d

# Start with rebuild
docker-compose --profile all up -d --build

# Stop all services
docker-compose --profile all down

# View logs
docker logs stockSimulator-<service-name> 2>&1 | tail -50

# Check container status
docker-compose --profile all ps

# Restart specific service
docker-compose --profile all restart <service-name>

# Reset Kafka (cluster ID mismatch)
docker-compose --profile all down
docker volume rm stock-simulator_kafka_data stock-simulator_zookeeper_data
docker-compose --profile all up -d
```

## Architecture

### Project Structure

```
Stock-Simulator/
├── doc/                    # Korean-language design documents
├── frontend/               # SvelteKit application
├── backend/                # Kotlin/Spring Boot MSA
│   ├── common/            # Shared DTOs, exceptions, events
│   ├── eureka-server/     # Service discovery (port 8761)
│   ├── api-gateway/       # API Gateway (port 8080 → 9832)
│   ├── user-service/      # Auth, users (port 8081)
│   ├── stock-service/     # Stocks, prices (port 8082)
│   ├── trading-service/   # Orders, portfolio (port 8083)
│   ├── event-service/     # Game events (port 8084)
│   ├── scheduler-service/ # NPC trading (port 8085)
│   ├── news-service/      # News articles (port 8086)
│   └── season-service/    # Rankings (port 8087)
├── infra/                  # Prometheus, Grafana configs
│   ├── grafana/provisioning/
│   │   ├── dashboards/    # Dashboard JSON files
│   │   └── datasources/   # Datasource configs
│   └── prometheus/        # Prometheus config
└── docker-compose.yml
```

### Backend Service Configuration

Each service has two config files:
- `application.yml` - Local development (localhost)
- `application-docker.yml` - Docker environment (uses env variables)

**Environment Variables (`.env` file):**
```env
# Infrastructure hosts
EUREKA_HOST=172.30.1.79
POSTGRES_HOST=172.30.1.79
REDIS_HOST=172.30.1.79
MONGO_HOST=172.30.1.79
KAFKA_HOST=172.30.1.79
ELASTICSEARCH_HOST=172.30.1.79

# Credentials
POSTGRES_USER=stocksim
POSTGRES_PASSWORD=stocksim123
REDIS_PASSWORD=stocksim123
MONGO_USER=stocksim
MONGO_PASSWORD=stocksim123

# Spring profile
SPRING_PROFILES_ACTIVE=docker
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
- **PostgreSQL**: Single DB with schema separation (users, stocks, trading, events, scheduler, season)
  - Primary + Replica (Streaming Replication) for Write/Read separation
- **MongoDB**: event (logs), news
- **Redis/Redisson**: stock (prices), trading (orderbook), season (ranking)
- **Kafka**: inter-service events

## Docker Infrastructure

| Service | Container | External Port | Credentials |
|---------|-----------|---------------|-------------|
| PostgreSQL | stockSimulator-postgres | 5432 | user: `stocksim`, pw: `stocksim123` |
| PostgreSQL Replica | stockSimulator-postgres-replica | 5433 | same as primary |
| MongoDB | stockSimulator-mongo | 27018 | user: `stocksim`, pw: `stocksim123` |
| Redis | stockSimulator-redis | 6380 | pw: `stocksim123` |
| Kafka | stockSimulator-kafka | 9093 | - |
| Kafka UI | stockSimulator-kafka-ui | 8089 | http://localhost:8089 |
| Elasticsearch | stockSimulator-elasticsearch | 9201 | - |
| Prometheus | stockSimulator-prometheus | 9091 | http://localhost:9091 |
| Grafana | stockSimulator-grafana | 3001 | admin/stocksim123 |
| Eureka | stockSimulator-eureka-server | 8761 | http://localhost:8761 |
| API Gateway | stockSimulator-api-gateway | 9832 | - |

## Monitoring

### Prometheus Targets
Check service health: `http://localhost:9091/targets`

All services expose metrics at `/actuator/prometheus`

### Grafana Dashboard
- URL: http://localhost:3001
- Login: admin / stocksim123
- Dashboard: "Stock Simulator - Services Overview"
  - Service status (UP/DOWN)
  - Request rate per service
  - Response time (p95)
  - JVM memory usage
  - CPU usage
  - Thread count
  - DB connection pool

## Known Issues & Solutions

### 1. Kafka Cluster ID Mismatch
**Error:** `InconsistentClusterIdException`
**Solution:**
```bash
docker-compose --profile all down
docker volume rm stock-simulator_kafka_data stock-simulator_zookeeper_data
docker-compose --profile all up -d
```

### 2. Eureka Server Starting on Wrong Port
**Cause:** Missing `application.yml` or `application-docker.yml`
**Solution:** Ensure `SERVER_PORT` env variable is set in docker-compose.yml

### 3. Services Can't Connect to Eureka
**Cause:** Using `localhost` instead of Docker service name
**Solution:** Use `application-docker.yml` with `eureka-server:8761`

## Key Documentation

Design specifications in `doc/`:
- `모의주식게임_기획서_v1.0.md` - Feature spec
- `모의주식게임_개발로드맵.md` - Development roadmap
- `인프라_구축_진행상황.md` - Infrastructure setup progress
- `SVELTEKIT_DEVELOPMENT_TEMPLATE.md` - Frontend guidelines

## Code Style

- **Formatting:** Prettier with tabs, single quotes, 100 char line width
- **TypeScript:** Strict mode enabled
- **Svelte:** Uses Svelte 5 runes syntax
- **Kotlin:** Standard Kotlin conventions

## Frontend Development Rules

### Critical CSS Rules
- **All styles must be in separate CSS files** (`src/styles/`) - NO `<style>` tags in Svelte components
- Use CSS variables for all colors
- Support dark/light mode via CSS variables

### Mobile/Desktop Routing
- Desktop routes: `/페이지명` (e.g., `/dashboard`)
- Mobile routes: `/m/페이지명` (e.g., `/m/dashboard`)

### API Communication
- All responses follow `ApiResponse<T>` structure
- Use `$lib/api/api.ts` for all API calls
- Before backend: use mock data with `VITE_USE_MOCK=true`
