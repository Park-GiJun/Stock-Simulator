# GitHub Actions 설정 가이드

## 📋 개요

Stock-Simulator 프로젝트의 GitHub Actions CI/CD 파이프라인 설정 가이드입니다.

**환경:**
- 단일 프로덕션 서버: 172.30.1.79 (gijunpark 사용자)
- 도메인: https://gijun.net (Frontend), https://api.gijun.net (API)
- Docker Compose 기반 마이크로서비스 배포

**워크플로우:**
1. `ci.yml` - CI 테스트 (모든 브랜치)
2. `deploy-production.yml` - 프로덕션 배포 (태그 v*.*.*)
3. `rollback.yml` - 수동 롤백
4. `security-scan.yml` - 보안 스캔 (기존 유지)

---

## 🔐 1단계: GitHub Secrets 설정

배포를 위해 다음 Secrets를 GitHub Repository에 설정해야 합니다:

### GitHub Repository → Settings → Secrets and variables → Actions

#### 필수 Secrets

| Secret 이름 | 설명 | 예시 |
|------------|------|------|
| `SSH_PRIVATE_KEY` | 서버 SSH private key | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

#### 선택 Secrets (알림용)

| Secret 이름 | 설명 |
|------------|------|
| `SLACK_WEBHOOK_URL` | Slack 알림 Webhook URL (선택) |

---

## 🔑 2단계: SSH Key 생성 및 설정

### 서버에서 SSH Key 생성

```bash
# 서버에 SSH 접속
ssh gijunpark@172.30.1.79

# SSH key pair 생성 (이미 있다면 생략)
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions

# Public key를 authorized_keys에 추가
cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Private key 내용 복사 (이것을 GitHub Secret에 등록)
cat ~/.ssh/github_actions
```

### GitHub에 Private Key 등록

1. GitHub Repository → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret** 클릭
3. Name: `SSH_PRIVATE_KEY`
4. Value: 위에서 복사한 private key 전체 내용 (-----BEGIN부터 -----END까지)
5. **Add secret** 클릭

---

## 🌍 3단계: GitHub Environments 설정

### Production Environment

1. GitHub Repository → **Settings** → **Environments**
2. **New environment** 클릭
3. Name: `production`
4. **Configure environment** 클릭
5. **Environment protection rules** 설정:
   - ☑️ **Required reviewers**: 배포 승인자 추가 (예: 본인)
   - ☑️ **Wait timer**: 5분 (선택사항)
6. **Save protection rules** 클릭

### Production Approval Environment

1. **New environment** 클릭
2. Name: `production-approval`
3. **Configure environment** 클릭
4. **Environment protection rules** 설정:
   - ☑️ **Required reviewers**: 배포 승인자 추가
5. **Save protection rules** 클릭

### Production Rollback Environment

1. **New environment** 클릭
2. Name: `production-rollback`
3. **Configure environment** 클릭
4. **Environment protection rules** 설정:
   - ☑️ **Required reviewers**: 롤백 승인자 추가
5. **Save protection rules** 클릭

---

## 📂 4단계: 서버 디렉토리 설정

서버에 프로젝트 디렉토리 생성 및 초기 설정:

```bash
# 서버 접속
ssh gijunpark@172.30.1.79

# 프로젝트 디렉토리로 이동 (이미 있다면 생략)
cd ~
git clone <your-repo-url> Stock-Simulator
cd Stock-Simulator

# .env 파일 확인 및 수정
cp .env .env.backup
nano .env

# 백업 디렉토리 생성
mkdir -p ~/production-backups
mkdir -p ~/rollback-backups

# Docker 및 Docker Compose 설치 확인
docker --version
docker-compose --version
```

### .env 파일 설정 확인

`.env` 파일이 다음과 같이 설정되어 있는지 확인:

```env
# Infrastructure Hosts
INFRA_HOST=172.30.1.79
EUREKA_HOST=172.30.1.79
POSTGRES_HOST=172.30.1.79
REDIS_HOST=172.30.1.79
MONGO_HOST=172.30.1.79
KAFKA_HOST=172.30.1.79
ELASTICSEARCH_HOST=172.30.1.79

# PostgreSQL
POSTGRES_USER=stocksim
POSTGRES_PASSWORD=stocksim123
POSTGRES_DB=stocksimulator
POSTGRES_PORT=5432

# MongoDB
MONGO_USER=stocksim
MONGO_PASSWORD=stocksim123

# Redis
REDIS_PASSWORD=stocksim123

# Application URLs
API_URL=https://api.gijun.net
FRONTEND_URL=https://gijun.net

# Spring Profiles
SPRING_PROFILES_ACTIVE=docker
```

---

## 🚀 5단계: 배포 플로우

### CI (자동 실행)

모든 push/PR에서 자동으로 실행됩니다:

```yaml
Trigger: Push to any branch or Pull Request
Jobs:
  1. Backend Tests (PostgreSQL, MongoDB, Redis)
  2. Backend Build (All services)
  3. Frontend Lint & Type Check
  4. Frontend Build
  5. CI Summary
```

### Production 배포 (수동 승인 필요)

#### 방법 1: Git Tag로 배포 (권장)

```bash
# 로컬에서 태그 생성
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# GitHub Actions 자동 시작
# → Manual Approval 단계에서 승인 대기
# → Approve 후 배포 진행
```

#### 방법 2: Manual Dispatch

1. GitHub Repository → **Actions** → **Deploy to Production**
2. **Run workflow** 클릭
3. Version 입력 (예: `v1.0.0`)
4. **Run workflow** 클릭
5. Manual Approval 승인
6. 배포 진행

#### 배포 흐름

```yaml
1. Validate (버전 검증)
2. Build & Push Docker Images (ghcr.io)
3. 🚨 Manual Approval (수동 승인 필요)
4. Backup Production (DB, 설정 백업)
5. Deploy (Rolling Update)
   - Eureka Server → Backend Services → API Gateway → Frontend
6. Health Check (내부 헬스체크)
7. Smoke Test (외부 URL 테스트)
8. ❌ 실패시 Auto Rollback
9. ✅ Deployment Summary
```

---

## 🔄 6단계: 롤백

문제 발생시 수동으로 롤백할 수 있습니다:

### 롤백 실행

1. GitHub Repository → **Actions** → **Rollback Deployment**
2. **Run workflow** 클릭
3. 입력:
   - **version**: 롤백할 버전 (예: `v1.0.0`)
   - **reason**: 롤백 사유
4. **Run workflow** 클릭
5. Manual Approval 승인
6. 롤백 진행

### 롤백 흐름

```yaml
1. Validate (이미지 존재 확인)
2. Backup Current State
3. 🚨 Manual Approval (수동 승인 필요)
4. Execute Rollback (이전 버전으로 복구)
5. Health Check
6. Notification
```

---

## 📊 7단계: 모니터링

### GitHub Actions 로그

- Repository → **Actions** → 각 워크플로우 클릭
- 각 Job의 로그 확인
- Summary에서 배포 결과 확인

### 서버 로그 확인

```bash
# 서버 접속
ssh gijunpark@172.30.1.79
cd ~/Stock-Simulator

# 컨테이너 상태 확인
docker-compose --profile all ps

# 특정 서비스 로그 확인
docker logs stockSimulator-eureka-server -f
docker logs stockSimulator-api-gateway -f
docker logs stockSimulator-frontend -f

# 전체 서비스 로그
docker-compose --profile all logs -f --tail=50
```

### Grafana 대시보드

- URL: http://172.30.1.79:3001
- 로그인: admin / stocksim123
- Dashboard: "Stock Simulator - Services Overview"

---

## 🔧 8단계: 트러블슈팅

### 배포 실패시

1. **GitHub Actions 로그 확인**
   - 어느 단계에서 실패했는지 확인
   - Error 메시지 확인

2. **서버 상태 확인**
   ```bash
   ssh gijunpark@172.30.1.79
   docker-compose --profile all ps
   docker logs <container-name>
   ```

3. **자동 롤백 실패시**
   - Manual Rollback 워크플로우 실행
   - 또는 서버에서 직접 복구:
     ```bash
     cd ~/Stock-Simulator
     # 최신 백업 확인
     ls -lt ~/production-backups/
     
     # 백업에서 복구 (예시)
     BACKUP_DIR=~/production-backups/<latest-backup>
     cp $BACKUP_DIR/.env .env
     docker-compose --profile all down
     docker-compose --profile all up -d
     ```

### SSH 접속 실패

- SSH key가 올바르게 설정되었는지 확인
- 서버 방화벽 설정 확인
- SSH 포트가 열려있는지 확인 (기본 22)

### Docker 이미지 Pull 실패

- GitHub Token 권한 확인
- GitHub Container Registry 접근 권한 확인
- 서버에서 수동으로 로그인 테스트:
  ```bash
  echo $GITHUB_TOKEN | docker login ghcr.io -u <username> --password-stdin
  ```

### 헬스체크 실패

- 서비스가 정상적으로 시작되었는지 확인
- Eureka에 서비스가 등록되었는지 확인
- 로그에서 에러 메시지 확인
- 대기 시간이 충분한지 확인 (현재 60초)

---

## 📝 9단계: 체크리스트

배포 전 확인사항:

- [ ] GitHub Secrets 설정 완료 (`SSH_PRIVATE_KEY`)
- [ ] GitHub Environments 설정 완료 (production, production-approval, production-rollback)
- [ ] 서버 SSH 접근 확인 (`ssh gijunpark@172.30.1.79`)
- [ ] 서버에 Docker & Docker Compose 설치 확인
- [ ] 서버에 ~/Stock-Simulator 디렉토리 존재
- [ ] .env 파일 설정 확인
- [ ] 백업 디렉토리 생성 완료
- [ ] GitHub Container Registry 권한 확인

---

## 🎯 10단계: 첫 배포

모든 설정이 완료되었다면 첫 배포를 시작하세요:

```bash
# 1. 로컬에서 태그 생성
git tag -a v1.0.0 -m "Initial production release"
git push origin v1.0.0

# 2. GitHub Actions 확인
# https://github.com/<your-repo>/actions

# 3. Manual Approval 승인

# 4. 배포 완료 후 확인
# Frontend: https://gijun.net
# API: https://api.gijun.net/actuator/health
# Grafana: http://172.30.1.79:3001
```

---

## 📚 참고 자료

- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

## 💬 문의

문제가 발생하거나 도움이 필요하면 GitHub Issues에 문의하세요.
