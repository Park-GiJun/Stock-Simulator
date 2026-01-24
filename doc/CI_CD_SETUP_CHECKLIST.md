# CI/CD 자동화 구축 완료 체크리스트

## ✅ 완료된 작업

### 1. CI 워크플로우 개선
- [x] `ci-backend.yml` - PostgreSQL로 변경 (MySQL → PostgreSQL + MongoDB)
- [x] `ci-frontend.yml` - 기존 유지 (이미 완벽)

### 2. 보안 스캔 추가
- [x] `security-scan.yml` - 신규 생성
  - Trivy Container Scan (전체 서비스)
  - Trivy Code Scan
  - OWASP Dependency Check (Backend)
  - npm audit (Frontend)
  - CodeQL Analysis (Java/Kotlin, JavaScript/TypeScript)
  - 매주 월요일 자동 실행

### 3. 롤백 워크플로우
- [x] `rollback.yml` - 신규 생성
  - 수동 실행만 가능
  - 환경별 롤백 (dev/staging/prod)
  - 이미지 검증
  - 현재 상태 백업
  - Health Check
  - Slack 알림

### 4. 환경별 배포 워크플로우
- [x] `deploy-dev.yml` - Development 자동 배포
  - develop 브랜치 푸시 시 자동
  - Docker 이미지: `dev-latest`, `dev-{SHA}`
  
- [x] `deploy-staging.yml` - Staging 자동 배포
  - main 브랜치 푸시 시 자동
  - Smoke Test + Integration Test
  - 순차적 서비스 재시작 (Zero-downtime)
  - Docker 이미지: `staging-latest`, `latest`
  
- [x] `deploy-prod.yml` - Production 수동 승인 배포
  - v*.*.* 태그 생성 시
  - 수동 승인 필수
  - Production 백업
  - Blue-Green 배포
  - 종합 Health Check
  - 실패 시 자동 롤백
  - Docker 이미지: `v{VERSION}`

### 5. 문서화
- [x] `doc/CI_CD_GUIDE.md` - 완전한 CI/CD 가이드 (18KB)
- [x] `doc/CI_CD_README.md` - 빠른 시작 가이드 (5KB)

---

## 🔧 필수 설정 작업

### 1. GitHub Secrets 등록

**Settings** → **Secrets and variables** → **Actions** → **New repository secret**

#### 필수 Secrets
```bash
SSH_PRIVATE_KEY         # SSH 개인키 전체 내용
```

#### 환경별 Secrets (서버가 분리되어 있는 경우)
```bash
# Development
DEV_SERVER_HOST         # 예: dev.stocksim.com 또는 IP
DEV_SERVER_USER         # 예: ubuntu (옵션, SERVER_USER 사용 가능)

# Staging
STAGING_SERVER_HOST     # 예: staging.stocksim.com
STAGING_SERVER_USER     # 예: ubuntu (옵션)

# Production
PROD_SERVER_HOST        # 예: stocksim.com
PROD_SERVER_USER        # 예: ubuntu (옵션)

# 또는 공통 (단일 서버인 경우)
SERVER_HOST             # Production fallback
SERVER_USER             # 모든 환경 fallback
```

#### 선택사항
```bash
SLACK_WEBHOOK_URL       # Slack Incoming Webhook (알림용)
```

### 2. GitHub Variables 설정

**Settings** → **Secrets and variables** → **Actions** → **Variables 탭**

```bash
# API URLs (프론트엔드 빌드용)
DEV_API_URL             # 예: http://dev-api.stocksim.com:9832
STAGING_API_URL         # 예: http://staging-api.stocksim.com:9832
VITE_API_URL            # 예: https://api.stocksim.com

# Service URLs (배포 후 확인용)
DEV_URL                 # 예: http://dev.stocksim.com
STAGING_URL             # 예: http://staging.stocksim.com
PROD_URL                # 예: https://stocksim.com

# Slack (Variable로도 가능)
SLACK_WEBHOOK_URL       # Slack Webhook URL
```

### 3. GitHub Environments 생성

**Settings** → **Environments** → **New environment**

#### 생성할 환경:
1. **development**
   - Protection rules: 없음 (완전 자동)

2. **staging**
   - Protection rules: 없음 (완전 자동)

3. **production**
   - ✅ Required reviewers: 팀 리더 추가
   - ✅ Wait timer: 5분 (옵션)
   - Deployment branch: `refs/tags/v*`

4. **production-approval** (중요!)
   - ✅ Required reviewers: 최소 1명 추가
   - Deployment branch: `refs/tags/v*`

### 4. SSH 키 설정

#### 로컬에서 SSH 키 생성
```bash
# 새 SSH 키 생성
ssh-keygen -t rsa -b 4096 -C "github-actions-stock-simulator" -f ~/.ssh/github_actions_rsa

# Private Key 출력 (GitHub Secret에 등록)
cat ~/.ssh/github_actions_rsa

# Public Key 출력 (서버에 등록)
cat ~/.ssh/github_actions_rsa.pub
```

#### 서버에 Public Key 등록
```bash
# 각 서버에 SSH 접속
ssh user@dev-server
ssh user@staging-server
ssh user@prod-server

# Public key 추가
echo "ssh-rsa AAAA... github-actions-stock-simulator" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

### 5. 서버 디렉토리 구조 준비

각 환경 서버에서:

```bash
# Development
mkdir -p ~/Stock-Simulator-dev
cd ~/Stock-Simulator-dev
git clone https://github.com/YOUR_ORG/Stock-Simulator.git .
git checkout develop

# Staging
mkdir -p ~/Stock-Simulator-staging
cd ~/Stock-Simulator-staging
git clone https://github.com/YOUR_ORG/Stock-Simulator.git .
git checkout main

# Production
mkdir -p ~/Stock-Simulator
cd ~/Stock-Simulator
git clone https://github.com/YOUR_ORG/Stock-Simulator.git .
git checkout main

# 각 서버에 .env 파일 생성
cp .env.example .env
# .env 파일 수정 (실제 환경에 맞게)
```

### 6. Slack 알림 설정 (선택사항)

1. Slack 워크스페이스에서 App 생성
2. **Incoming Webhooks** 활성화
3. 알림 받을 채널 선택 (예: #deployments)
4. Webhook URL 복사
5. GitHub Secrets 또는 Variables에 등록

---

## 🧪 테스트 방법

### 1. CI 워크플로우 테스트

```bash
# Backend CI 테스트
git checkout -b test/ci-backend
echo "# test" >> backend/README.md
git add . && git commit -m "test: CI backend"
git push origin test/ci-backend

# Frontend CI 테스트
git checkout -b test/ci-frontend
echo "# test" >> frontend/README.md
git add . && git commit -m "test: CI frontend"
git push origin test/ci-frontend

# Actions 탭에서 결과 확인
```

### 2. Security Scan 테스트

```bash
# GitHub Actions 탭
Actions → Security Scan → Run workflow → Run workflow
```

### 3. Development 배포 테스트

```bash
# develop 브랜치에 푸시
git checkout develop
echo "# test deploy dev" >> README.md
git add . && git commit -m "test: deploy dev"
git push origin develop

# Actions 탭에서 확인
# 서버에서 확인:
ssh user@dev-server
cd ~/Stock-Simulator-dev
docker-compose --profile all ps
```

### 4. Staging 배포 테스트

```bash
# main 브랜치에 머지
git checkout main
git merge develop
git push origin main

# Actions 탭에서 확인
```

### 5. Rollback 테스트

```bash
# GitHub Actions 탭
Actions → Rollback Deployment → Run workflow

# 입력:
Environment: dev
Version: dev-latest
Services: api-gateway
Reason: Testing rollback workflow
```

---

## 📋 배포 체크리스트

### 첫 배포 전 확인사항

- [ ] GitHub Secrets 모두 등록됨
- [ ] GitHub Environments 생성됨
- [ ] Production Approval 설정됨
- [ ] SSH 키 설정 완료
- [ ] 서버에 Git 저장소 클론됨
- [ ] 서버에 .env 파일 생성됨
- [ ] 서버에 Docker 설치됨
- [ ] 서버에 docker-compose 설치됨
- [ ] Slack 알림 설정됨 (선택)

### Development 배포 체크리스트

- [ ] develop 브랜치에 코드 푸시
- [ ] CI 통과 확인
- [ ] Security Scan 통과 확인
- [ ] 자동 배포 완료 확인
- [ ] Health Check 통과 확인
- [ ] 서비스 정상 작동 확인

### Staging 배포 체크리스트

- [ ] develop → main 머지
- [ ] CI 통과 확인
- [ ] Security Scan 통과 확인
- [ ] Smoke Test 통과 확인
- [ ] Integration Test 통과 확인
- [ ] 자동 배포 완료 확인
- [ ] Comprehensive Health Check 통과
- [ ] 서비스 정상 작동 확인

### Production 배포 체크리스트

- [ ] Staging에서 충분히 테스트됨
- [ ] 버전 태그 생성 (v*.*.*)
- [ ] CI/CD 워크플로우 시작 확인
- [ ] 이미지 검증 통과
- [ ] 수동 승인 대기
- [ ] **승인 클릭** (production-approval)
- [ ] Production 백업 완료 확인
- [ ] Blue-Green 배포 진행 확인
- [ ] Comprehensive Health Check 통과
- [ ] Smoke Test 통과
- [ ] 모니터링 정상 확인 (Grafana)
- [ ] 사용자 영향 없음 확인
- [ ] Slack 알림 확인

---

## 🚨 긴급 롤백 절차

Production에서 문제 발생 시:

### 즉시 조치
```bash
# 1. GitHub Actions 탭
Actions → Rollback Deployment → Run workflow

# 2. 입력
Environment: prod
Version: v1.0.0  # 이전 안정 버전
Services: all
Reason: Critical bug - [간단한 설명]

# 3. Run workflow 클릭

# 4. 롤백 진행 상황 모니터링
# 5. Health Check 통과 확인
```

### 롤백 후
1. 팀에 알림 (Slack, Email)
2. 원인 분석 시작
3. GitHub Issue 생성
4. 수정 후 재배포 계획

---

## 📞 지원

- **문서**: [CI/CD 완전 가이드](doc/CI_CD_GUIDE.md)
- **이슈**: GitHub Issues (Label: `ci/cd`)
- **긴급**: Slack #devops-alert

---

## 🎉 다음 단계

CI/CD 기본 구축 완료! 이제 다음을 고려하세요:

### Phase 2 (추가 최적화)
- [ ] Performance Testing (K6, JMeter)
- [ ] E2E Testing (Playwright, Cypress)
- [ ] SonarQube 통합
- [ ] Kubernetes 마이그레이션
- [ ] ArgoCD GitOps

**완료 날짜**: 2024-XX-XX  
**담당자**: DevOps Team
