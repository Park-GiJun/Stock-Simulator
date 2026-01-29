# GitHub Actions 구성 가이드

> **Stock-Simulator 프로젝트를 위한 GitHub Actions 완벽 가이드**

## 📋 목차

1. [현재 워크플로우 구조](#현재-워크플로우-구조)
2. [초기 설정 (Setup)](#초기-설정-setup)
3. [GitHub Secrets 설정](#github-secrets-설정)
4. [GitHub Variables 설정](#github-variables-설정)
5. [워크플로우 상세 설명](#워크플로우-상세-설명)
6. [트러블슈팅](#트러블슈팅)
7. [최적화 팁](#최적화-팁)

---

## 현재 워크플로우 구조

```
.github/workflows/
├── ci-backend.yml          # 백엔드 CI (테스트 + 빌드)
├── ci-frontend.yml         # 프론트엔드 CI (린트 + 빌드)
├── deploy-dev.yml          # 개발 서버 배포
├── deploy-staging.yml      # 스테이징 서버 배포
├── deploy-prod.yml         # 프로덕션 서버 배포
├── rollback.yml            # 롤백 워크플로우
└── security-scan.yml       # 보안 스캔 (Trivy, CodeQL, OWASP)
```

### 워크플로우 트리거 매트릭스

| 워크플로우 | Push (main) | Push (develop) | PR | Schedule | Manual |
|-----------|-------------|----------------|-----|----------|--------|
| ci-backend | ✅ | ✅ | ✅ | ❌ | ❌ |
| ci-frontend | ✅ | ✅ | ✅ | ❌ | ❌ |
| deploy-dev | ❌ | ✅ | ❌ | ❌ | ✅ |
| deploy-staging | ✅ | ❌ | ❌ | ❌ | ✅ |
| deploy-prod | ✅ (태그) | ❌ | ❌ | ❌ | ✅ |
| security-scan | ✅ | ✅ | ✅ | 주간 | ✅ |

---

## 초기 설정 (Setup)

### 1. GitHub Container Registry (GHCR) 활성화

```bash
# 1. GitHub 설정 → Packages → Package settings
# 2. "Improve Container Registry Support" 활성화
# 3. 리포지토리 visibility 설정
```

### 2. GitHub Packages 권한 설정

**Settings → Actions → General → Workflow permissions**
- ✅ Read and write permissions
- ✅ Allow GitHub Actions to create and approve pull requests

### 3. Environments 생성

**Settings → Environments → New environment**

#### Development
```yaml
Name: development
Protection rules: 
  - Required reviewers: 0 (자동 배포)
Environment secrets: (아래 참조)
Environment variables: (아래 참조)
```

#### Staging
```yaml
Name: staging
Protection rules: 
  - Required reviewers: 1
  - 특정 브랜치만 허용: main
Environment secrets: (아래 참조)
Environment variables: (아래 참조)
```

#### Production
```yaml
Name: production
Protection rules: 
  - Required reviewers: 2
  - Wait timer: 30분
  - 특정 브랜치만 허용: main
Environment secrets: (아래 참조)
Environment variables: (아래 참조)
```

---

## GitHub Secrets 설정

**Settings → Secrets and variables → Actions → Secrets**

### Repository Secrets (모든 환경 공통)

```bash
# SSH 접속
SSH_PRIVATE_KEY           # SSH 프라이빗 키
SERVER_USER              # SSH 사용자명 (기본값)

# Slack 알림 (선택사항)
SLACK_WEBHOOK_URL        # Slack Incoming Webhook URL

# Container Registry (자동 생성됨)
GITHUB_TOKEN            # 자동 제공, 설정 불필요
```

### Environment Secrets (환경별)

#### development
```bash
DEV_SERVER_HOST          # 개발 서버 IP/도메인
DEV_SERVER_USER         # 개발 서버 SSH 사용자 (선택, 기본값은 SERVER_USER)
```

#### staging
```bash
STAGING_SERVER_HOST      # 스테이징 서버 IP/도메인
STAGING_SERVER_USER     # 스테이징 서버 SSH 사용자
```

#### production
```bash
PROD_SERVER_HOST         # 프로덕션 서버 IP/도메인
PROD_SERVER_USER        # 프로덕션 서버 SSH 사용자
```

### SSH Private Key 생성 방법

```bash
# 1. SSH 키페어 생성
ssh-keygen -t ed25519 -C "github-actions@stocksimulator" -f ~/.ssh/github-actions

# 2. 공개키를 서버에 등록
ssh-copy-id -i ~/.ssh/github-actions.pub user@server-ip

# 3. 프라이빗 키 내용을 GitHub Secrets에 등록
cat ~/.ssh/github-actions
# -----BEGIN OPENSSH PRIVATE KEY----- 부터 끝까지 전체 복사
```

---

## GitHub Variables 설정

**Settings → Secrets and variables → Actions → Variables**

### Repository Variables

```bash
# 공통 설정
REGISTRY=ghcr.io
IMAGE_PREFIX=<your-github-username>/stocksim
```

### Environment Variables

#### development
```bash
DEV_URL=http://dev.stocksimulator.com
DEV_API_URL=http://dev-api.stocksimulator.com:9832
```

#### staging
```bash
STAGING_URL=https://staging.stocksimulator.com
STAGING_API_URL=https://staging-api.stocksimulator.com
```

#### production
```bash
PROD_URL=https://stocksimulator.com
PROD_API_URL=https://api.stocksimulator.com
```

---

## 워크플로우 상세 설명

### 1. CI - Backend (`ci-backend.yml`)

**목적**: 백엔드 코드 품질 검증 및 빌드

#### 트리거 조건
```yaml
push:
  branches: [main, develop]
  paths:
    - 'backend/**'
    - '.github/workflows/ci-backend.yml'
pull_request:
  branches: [main, develop]
  paths:
    - 'backend/**'
```

#### Job 구조
```
┌─────────────┐
│    Test     │  PostgreSQL, MongoDB, Redis 서비스와 함께 테스트 실행
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    Build    │  각 서비스별 병렬 빌드 (9개 서비스)
└─────────────┘
```

#### 테스트 환경
- PostgreSQL 16
- MongoDB 7
- Redis 7

#### 병렬 빌드 서비스
- eureka-server
- api-gateway
- user-service
- stock-service
- trading-service
- event-service
- scheduler-service
- news-service
- ~~season-service~~ (제거됨)

### 2. CI - Frontend (`ci-frontend.yml`)

**목적**: 프론트엔드 코드 품질 검증 및 빌드

#### Job 구조
```
┌──────────────────┐
│ Lint & Type Check│  ESLint + Svelte Check
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│      Build       │  SvelteKit 프로덕션 빌드
└──────────────────┘
```

#### 캐싱 최적화
- pnpm store 캐싱
- Node.js modules 캐싱

### 3. Deploy to Development (`deploy-dev.yml`)

**목적**: develop 브랜치를 개발 서버에 자동 배포

#### 배포 프로세스

```
┌───────────────────┐
│ Build & Push      │  Docker 이미지 빌드 및 GHCR 푸시
│ (10개 서비스 병렬)  │  
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│      Deploy       │  SSH로 서버 접속 → docker-compose up
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│   Health Check    │  각 서비스 health endpoint 확인
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│      Notify       │  Slack 알림 (선택)
└───────────────────┘
```

#### Docker 이미지 태그 전략
```
dev-latest          # 항상 최신 개발 버전
dev-{git-sha}       # 특정 커밋 기반 이미지
```

#### Health Check 대상
- API Gateway (port 9832)
- Eureka Server (port 8761)
- Frontend (port 8080)

### 4. Security Scan (`security-scan.yml`)

**목적**: 보안 취약점 스캔 및 리포팅

#### 스캔 종류

##### Trivy Container Scan
- Docker 이미지 취약점 스캔
- CRITICAL, HIGH 심각도 우선
- SARIF 형식으로 GitHub Security 탭에 업로드

##### Trivy Code Scan
- 소스 코드 취약점 스캔
- 설정 파일, 의존성 등 검사

##### OWASP Dependency Check (Backend)
- Gradle 의존성 취약점 분석
- HTML 리포트 생성

##### npm audit (Frontend)
- npm 패키지 취약점 스캔
- JSON 리포트 생성

##### CodeQL Analysis
- GitHub의 정적 코드 분석
- Java/Kotlin, JavaScript/TypeScript 분석
- 보안 및 품질 쿼리 실행

#### 스캔 일정
- Push/PR 시: 코드 스캔만
- 매주 월요일 00:00 UTC: 전체 스캔
- 수동 트리거 가능

---

## 트러블슈팅

### 문제 1: SSH 연결 실패

```
Error: Permission denied (publickey)
```

**해결 방법:**
```bash
# 1. 서버의 authorized_keys 확인
cat ~/.ssh/authorized_keys

# 2. 공개키가 등록되어 있는지 확인
ssh-copy-id -i ~/.ssh/github-actions.pub user@server-ip

# 3. SSH 권한 확인
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

### 문제 2: Docker 로그인 실패

```
Error: denied: permission_denied
```

**해결 방법:**
```yaml
# Workflow permissions 확인
permissions:
  contents: read
  packages: write  # 이것이 있어야 함
```

### 문제 3: Gradle 빌드 실패 (season-service 관련)

```
Error: Project 'season-service' not found
```

**해결 방법:**
```yaml
# ci-backend.yml의 matrix에서 제거
strategy:
  matrix:
    service:
      - eureka-server
      # ... 
      - news-service
      # season-service 제거됨
```

### 문제 4: Health Check 타임아웃

```
Error: curl: (7) Failed to connect
```

**해결 방법:**
```bash
# 1. 서비스 시작 대기 시간 증가
- name: Wait for services
  run: sleep 60  # 30 → 60초로 증가

# 2. 서버에서 직접 확인
docker-compose --profile all ps
docker logs stockSimulator-api-gateway
```

### 문제 5: Out of Memory (빌드 중)

```
Error: Java heap space
```

**해결 방법:**
```yaml
# Gradle 메모리 설정
- name: Build with Gradle
  run: ./gradlew build -x test
  env:
    GRADLE_OPTS: "-Xmx4g -XX:MaxMetaspaceSize=512m"
```

---

## 최적화 팁

### 1. 캐싱 활용

#### Gradle 캐싱
```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: 'gradle'  # 자동 캐싱
```

#### Docker 레이어 캐싱
```yaml
- name: Build and push
  uses: docker/build-push-action@v5
  with:
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

#### pnpm 캐싱
```yaml
- name: Setup Node.js
  uses: actions/setup-node@v4
  with:
    cache: 'pnpm'
    cache-dependency-path: frontend/pnpm-lock.yaml
```

### 2. 병렬 처리

#### Matrix Strategy 사용
```yaml
strategy:
  matrix:
    service: [service1, service2, service3]
  # 기본적으로 병렬 실행됨
```

#### Concurrency 제어
```yaml
concurrency:
  group: deploy-${{ github.ref }}
  cancel-in-progress: true  # 이전 실행 취소
```

### 3. 조건부 실행

#### 경로 필터링
```yaml
on:
  push:
    paths:
      - 'backend/**'          # 백엔드 변경 시만
      - '!backend/**/README.md'  # README 제외
```

#### Job 조건
```yaml
jobs:
  deploy:
    if: github.ref == 'refs/heads/main'  # main 브랜치만
```

### 4. Artifacts 관리

#### 보존 기간 설정
```yaml
- name: Upload artifact
  uses: actions/upload-artifact@v4
  with:
    retention-days: 7  # 7일 후 자동 삭제
```

### 5. 비용 절감

#### Self-hosted Runners 고려
```yaml
jobs:
  build:
    runs-on: self-hosted  # 자체 러너 사용
    # 또는
    runs-on: [self-hosted, linux, x64]
```

#### 불필요한 실행 방지
```yaml
on:
  pull_request:
    paths-ignore:
      - '**.md'
      - 'doc/**'
      - '.gitignore'
```

---

## 실전 사용 예시

### 시나리오 1: 기능 개발 및 배포

```bash
# 1. 새 기능 브랜치 생성
git checkout -b feature/stock-listing
git push -u origin feature/stock-listing

# 2. PR 생성
# → ci-backend.yml, ci-frontend.yml 자동 실행
# → 테스트 통과 확인

# 3. develop 브랜치로 머지
git checkout develop
git merge feature/stock-listing
git push origin develop

# 4. 자동 배포
# → deploy-dev.yml 자동 실행
# → 개발 서버에 배포됨

# 5. 개발 서버 확인
curl http://dev.stocksimulator.com/actuator/health
```

### 시나리오 2: 프로덕션 배포

```bash
# 1. main 브랜치로 머지 (PR + 리뷰)
git checkout main
git merge develop
git push origin main

# 2. 릴리스 태그 생성
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 3. GitHub Actions 탭에서 deploy-prod 수동 실행
# → Production environment 승인 필요 (2명)
# → 30분 대기 타이머
# → 배포 실행

# 4. Health Check 확인
# → Slack 알림 수신
```

### 시나리오 3: 보안 스캔 결과 확인

```bash
# 1. Security 탭 이동
# GitHub Repository → Security → Code scanning alerts

# 2. Trivy 결과 확인
# Filter: Tool = Trivy

# 3. CodeQL 결과 확인
# Filter: Tool = CodeQL

# 4. 심각도 높은 것부터 수정
# Critical → High → Medium 순서
```

### 시나리오 4: 배포 롤백

```bash
# 1. GitHub Actions 탭에서 rollback.yml 실행
# → Environment 선택: production
# → Rollback target: 이전 성공한 실행 번호 입력

# 2. 롤백 확인
# → Health Check 자동 실행
# → Slack 알림 수신

# 3. 또는 수동 롤백
ssh user@prod-server
cd ~/Stock-Simulator
git log --oneline
git reset --hard <previous-commit>
docker-compose --profile all up -d
```

---

## 추가 개선 사항

### 1. season-service 제거 반영

**ci-backend.yml 업데이트 필요:**
```yaml
strategy:
  matrix:
    service:
      - eureka-server
      - api-gateway
      - user-service
      - stock-service
      - trading-service
      - event-service
      - scheduler-service
      - news-service
      # season-service 제거됨
```

**모든 deploy-*.yml 업데이트 필요:**
동일하게 season-service 제거

### 2. Kotlin 2.3.0 반영

**ci-backend.yml 업데이트:**
```yaml
env:
  JAVA_VERSION: '21'
  KOTLIN_VERSION: '2.3.0'  # 추가
  GRADLE_VERSION: '8.5'
```

### 3. 성능 모니터링 추가

**배포 후 성능 체크:**
```yaml
- name: Performance check
  run: |
    # Prometheus metrics 확인
    curl http://localhost:9091/api/v1/query?query=up
    
    # Response time 확인
    curl -w "@curl-format.txt" -o /dev/null -s http://localhost:9832/actuator/health
```

---

## 체크리스트

### 초기 설정 ✅
- [ ] GitHub Packages 활성화
- [ ] Workflow permissions 설정
- [ ] Development environment 생성
- [ ] Staging environment 생성
- [ ] Production environment 생성

### Secrets 설정 ✅
- [ ] SSH_PRIVATE_KEY 등록
- [ ] SERVER_USER 등록
- [ ] DEV_SERVER_HOST 등록
- [ ] STAGING_SERVER_HOST 등록
- [ ] PROD_SERVER_HOST 등록
- [ ] SLACK_WEBHOOK_URL 등록 (선택)

### Variables 설정 ✅
- [ ] DEV_URL 등록
- [ ] DEV_API_URL 등록
- [ ] STAGING_URL 등록
- [ ] STAGING_API_URL 등록
- [ ] PROD_URL 등록
- [ ] PROD_API_URL 등록

### 서버 준비 ✅
- [ ] SSH 공개키 등록
- [ ] Docker 설치
- [ ] Docker Compose 설치
- [ ] 프로젝트 클론
- [ ] .env 파일 설정

### 워크플로우 테스트 ✅
- [ ] CI 테스트 성공
- [ ] 개발 서버 배포 성공
- [ ] Health Check 통과
- [ ] 보안 스캔 실행

---

## 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Trivy](https://github.com/aquasecurity/trivy)
- [CodeQL](https://codeql.github.com/)

---

**작성일**: 2026년 1월 28일  
**버전**: 1.0  
**상태**: ✅ 완료
