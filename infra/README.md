# =============================================================================
# Stock-Simulator Infrastructure Documentation
# =============================================================================

# 🏗️ 인프라 아키텍처

## 📋 목차
1. [개요](#개요)
2. [Docker Compose 구성](#docker-compose-구성)
3. [서비스 구조](#서비스-구조)
4. [배포 가이드](#배포-가이드)
5. [운영 가이드](#운영-가이드)



## 개요

Stock-Simulator는 마이크로서비스 아키텍처 기반의 모의 주식 거래 게임입니다.

### 기술 스택
- **Container**: Docker & Docker Compose
- **Database**: PostgreSQL (Primary + Replica), MongoDB, Redis
- **Message Broker**: Apache Kafka
- **Search**: Elasticsearch
- **Monitoring**: Prometheus + Grafana
- **Reverse Proxy**: Nginx (외부 서버)

### 서버 정보
- **Server IP**: 172.30.1.79
- **Frontend**: https://gijun.net
- **API Gateway**: https://api.gijun.net
- **Grafana**: http://localhost:3001 (admin/stocksim123)
- **Kafka UI**: http://localhost:8089

---

## Docker Compose 구성

### Profile 구조

```bash
# 전체 인프라 시작 (DB, 메시지 브로커 등)
docker-compose --profile infra up -d

# 모니터링 시작
docker-compose --profile monitoring up -d

# 마이크로서비스 시작
docker-compose --profile services up -d

# 프론트엔드 시작
docker-compose --profile frontend up -d

# 전체 시작
docker-compose --profile all up -d

# 전체 중지
docker-compose down

# 볼륨 포함 삭제
docker-compose down -v
```

### 서비스 포트 맵핑

| 서비스 | 컨테이너명 | 외부 포트 | 접속 정보 |
|--------|-----------|----------|----------|
| PostgreSQL (Primary) | stockSimulator-postgres | 5432 | user: `stocksim`, pw: `stocksim123`, db: `stocksimulator` |
| PostgreSQL (Replica) | stockSimulator-postgres-replica | 5433 | user: `stocksim`, pw: `stocksim123` |
| MongoDB | stockSimulator-mongo | 27018 | user: `stocksim`, pw: `stocksim123` |
| Redis | stockSimulator-redis | 6380 | pw: `stocksim123` |
| Kafka | stockSimulator-kafka | 9093 | - |
| Kafka UI | stockSimulator-kafka-ui | 8089 | http://localhost:8089 |
| Elasticsearch | stockSimulator-elasticsearch | 9201 | - |
| Prometheus | stockSimulator-prometheus | 9091 | http://localhost:9091 |
| Grafana | stockSimulator-grafana | 3001 | http://localhost:3001 (admin/stocksim123) |
| Eureka Server | stockSimulator-eureka-server | 8761 | http://localhost:8761 |
| API Gateway | stockSimulator-api-gateway | 9832 | http://localhost:9832 |
| User Service | stockSimulator-user-service | 8081 | - |
| Stock Service | stockSimulator-stock-service | 8082 | - |
| Trading Service | stockSimulator-trading-service | 8083 | - |
| Event Service | stockSimulator-event-service | 8084 | - |
| Scheduler Service | stockSimulator-scheduler-service | 8085 | - |
| News Service | stockSimulator-news-service | 8086 | - |
| Season Service | stockSimulator-season-service | 8087 | - |
| Frontend | stockSimulator-frontend | 8080 | http://localhost:8080 |

---

## 서비스 구조

### 네트워크 아키텍처

```
                    ┌─────────────────┐
                    │     Nginx       │
                    │ (외부 서버)      │
                    │ gijun.net       │
                    │ api.gijun.net   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
        ┌─────────┐   ┌───────────┐   ┌─────────┐
        │Frontend │   │API Gateway│   │ Grafana │
        │ :8080   │   │  :9832    │   │ :3001   │
        └─────────┘   └─────┬─────┘   └─────────┘
                            │
           ┌────────────────┼────────────────┐
           │                │                │
           ▼                ▼                ▼
      ┌─────────┐     ┌──────────┐    ┌──────────┐
      │ Eureka  │◄────│ Services │────│  Kafka   │
      │ :8761   │     │8081-8087 │    │  :9093   │
      └─────────┘     └────┬─────┘    └──────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
         ┌───────┐   ┌─────────┐  ┌─────────┐
         │ Postgres│  │ MongoDB │  │  Redis  │
         │:5432/33│   │ :27018  │  │ :6380   │
         └───────┘   └─────────┘  └─────────┘
```

### DB 구조

#### PostgreSQL (스키마 분리)

| Schema | Service | Description |
|--------|---------|-------------|
| `users` | user-service | 회원, 인증 |
| `stocks` | stock-service | 종목, 시세 |
| `trading` | trading-service | 주문, 포트폴리오 |
| `events` | event-service | 게임 이벤트 |
| `scheduler` | scheduler-service | NPC 트레이딩 |
| `season` | season-service | 시즌, 랭킹 |

#### MongoDB
- 사용 서비스: news-service, event-service (로그)
- Database: `stocksimulator`

#### Redis
- 사용: 세션, 캐시, 실시간 데이터 (주가, 호가창, 랭킹)

---

## 배포 가이드

### 1. 사전 요구사항

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Docker Compose 설치 (Docker Desktop에는 포함됨)
sudo apt-get install docker-compose-plugin
```

### 2. 인프라 시작

```bash
# 저장소 클론
git clone https://github.com/YOUR_USERNAME/Stock-Simulator.git
cd Stock-Simulator

# 인프라 서비스 시작 (DB, Kafka, Redis 등)
docker-compose --profile infra up -d

# 모니터링 시작
docker-compose --profile monitoring up -d
```

### 3. 서비스 빌드 및 시작

```bash
# 백엔드 이미지 빌드
./infra/scripts/build-docker-images.sh

# 마이크로서비스 시작
docker-compose --profile services up -d

# 프론트엔드 시작
docker-compose --profile frontend up -d
```

### 4. 외부 접근 설정 (Nginx)

서버에서 Nginx를 리버스 프록시로 설정:

```bash
# Nginx 설정 복사
sudo cp infra/nginx/gijun.net /etc/nginx/sites-available/
sudo ln -s /etc/nginx/sites-available/gijun.net /etc/nginx/sites-enabled/

# Nginx 재시작
sudo nginx -t
sudo systemctl reload nginx
```

---

## 운영 가이드

### 컨테이너 상태 확인

```bash
# 전체 상태
docker-compose ps

# 특정 서비스 로그
docker-compose logs -f user-service

# 컨테이너 내부 접속
docker exec -it stockSimulator-postgres psql -U stocksim -d stocksimulator
```

### DB 관리

```bash
# PostgreSQL 접속
docker exec -it stockSimulator-postgres psql -U stocksim -d stocksimulator

# MongoDB 접속
docker exec -it stockSimulator-mongo mongosh -u stocksim -p stocksim123 --authenticationDatabase admin

# Redis 접속
docker exec -it stockSimulator-redis redis-cli -a stocksim123
```

### 서비스 재시작

```bash
# 특정 서비스 재시작
docker-compose restart user-service

# 서비스 재빌드 및 시작
docker-compose up -d --build user-service
```

### 스케일링

```bash
# 서비스 스케일 아웃
docker-compose up -d --scale user-service=3
```

### 로그 확인

```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f api-gateway user-service

# 최근 100줄
docker-compose logs --tail=100 user-service
```

---

## 트러블슈팅

### DB 연결 실패
```bash
# PostgreSQL 상태 확인
docker exec -it stockSimulator-postgres pg_isready -U stocksim

# 네트워크 확인
docker network inspect stock-simulator_stocksim-network
```

### 컨테이너 재시작 반복
```bash
# 로그 확인
docker logs stockSimulator-user-service

# 컨테이너 상세 정보
docker inspect stockSimulator-user-service
```

### 포트 충돌
```bash
# 사용 중인 포트 확인 (Linux)
sudo lsof -i :8080

# 사용 중인 포트 확인 (Windows)
netstat -ano | findstr :8080
```

---

## 설정 파일 구조

```
infra/
├── docker/               # Dockerfile 모음
│   ├── backend/          # 백엔드 서비스 Dockerfile
│   └── frontend/         # 프론트엔드 Dockerfile
├── grafana/
│   └── provisioning/     # Grafana 자동 설정
│       ├── dashboards/
│       └── datasources/
├── nginx/                # Nginx 설정
│   ├── gijun.net         # 메인 도메인 설정
│   └── nginx.conf        # Docker 내부용
├── postgres/
│   └── init-schemas.sql  # 스키마 초기화
├── prometheus/
│   ├── prometheus.yml        # K8s용 (deprecated)
│   └── prometheus-docker.yml # Docker용
├── scripts/
│   └── build-docker-images.sh # 이미지 빌드 스크립트
└── README.md             # 이 문서
```

---

## 연락처

문제 발생 시 이슈 생성: https://github.com/YOUR_USERNAME/Stock-Simulator/issues
