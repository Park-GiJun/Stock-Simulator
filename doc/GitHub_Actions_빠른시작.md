# GitHub Actions 구성 완료 요약

## ✅ 완료된 작업

### 1. **종합 가이드 작성**
- 📄 **파일**: `doc/GitHub_Actions_가이드.md`
- 📋 **내용**:
  - 워크플로우 구조 및 트리거 매트릭스
  - 초기 설정 단계별 가이드
  - GitHub Secrets/Variables 설정 방법
  - 각 워크플로우 상세 설명
  - 트러블슈팅 가이드
  - 최적화 팁
  - 실전 사용 예시

### 2. **season-service 제거**
모든 워크플로우에서 season-service 참조 제거:

- ✅ `.github/workflows/ci-backend.yml`
- ✅ `.github/workflows/deploy-dev.yml`
- ✅ `.github/workflows/deploy-staging.yml`
- ✅ `.github/workflows/deploy-prod.yml`
- ✅ `.github/workflows/security-scan.yml`
- ✅ `.github/workflows/rollback.yml`

### 3. **현재 서비스 목록 (9개)**
```
Backend (8):
- eureka-server
- api-gateway
- user-service
- stock-service
- trading-service
- event-service
- scheduler-service
- news-service

Frontend (1):
- frontend
```

---

## 🚀 빠른 시작 가이드

### Step 1: GitHub Secrets 설정

**Settings → Secrets and variables → Actions → Secrets**

```bash
# 필수 Secrets
SSH_PRIVATE_KEY           # SSH 프라이빗 키 전체
SERVER_USER              # SSH 사용자명

# 환경별 Secrets (Environments)
DEV_SERVER_HOST          # development 환경
STAGING_SERVER_HOST      # staging 환경
PROD_SERVER_HOST         # production 환경

# 선택 Secrets
SLACK_WEBHOOK_URL        # Slack 알림용
```

### Step 2: GitHub Variables 설정

**Settings → Secrets and variables → Actions → Variables**

```bash
# Environment Variables (development)
DEV_URL=http://dev.stocksimulator.com
DEV_API_URL=http://dev-api.stocksimulator.com:9832

# Environment Variables (staging)
STAGING_URL=https://staging.stocksimulator.com
STAGING_API_URL=https://staging-api.stocksimulator.com

# Environment Variables (production)
PROD_URL=https://stocksimulator.com
PROD_API_URL=https://api.stocksimulator.com
```

### Step 3: SSH 키 설정

```bash
# 1. 로컬에서 SSH 키 생성
ssh-keygen -t ed25519 -C "github-actions@stocksimulator" -f ~/.ssh/github-actions

# 2. 서버에 공개키 등록
ssh-copy-id -i ~/.ssh/github-actions.pub user@server-ip

# 3. 프라이빗 키를 GitHub Secrets에 등록
cat ~/.ssh/github-actions
# 출력된 내용 전체를 SSH_PRIVATE_KEY에 복사
```

### Step 4: Environments 생성

**Settings → Environments → New environment**

#### development
- Protection rules: 없음 (자동 배포)
- Secrets: `DEV_SERVER_HOST`, `DEV_SERVER_USER`
- Variables: `DEV_URL`, `DEV_API_URL`

#### staging
- Protection rules: Required reviewers (1명)
- Secrets: `STAGING_SERVER_HOST`, `STAGING_SERVER_USER`
- Variables: `STAGING_URL`, `STAGING_API_URL`

#### production
- Protection rules: 
  - Required reviewers (2명)
  - Wait timer (30분)
- Secrets: `PROD_SERVER_HOST`, `PROD_SERVER_USER`
- Variables: `PROD_URL`, `PROD_API_URL`

### Step 5: 서버 준비

```bash
# 각 환경 서버에서 실행

# 1. Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 2. 프로젝트 클론
git clone https://github.com/your-username/Stock-Simulator.git
cd Stock-Simulator

# 3. 환경별 브랜치 체크아웃
# Development: develop 브랜치
# Staging/Production: main 브랜치

# 4. .env 파일 설정
cp .env.example .env
# 환경에 맞게 수정

# 5. 초기 실행 테스트
docker-compose --profile all up -d
docker-compose --profile all ps
```

---

## 📊 워크플로우 실행 흐름

### 개발 워크플로우

```
┌─────────────────┐
│  Feature 개발   │
│  (feature/*)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   PR 생성       │  → CI 자동 실행
│  (→ develop)    │     (테스트 + 빌드)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  develop 머지   │  → 개발 서버 자동 배포
│                 │     (deploy-dev.yml)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Health Check   │  → Slack 알림
│                 │
└─────────────────┘
```

### 프로덕션 워크플로우

```
┌─────────────────┐
│  develop 테스트 │
│     완료        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   PR 생성       │  → CI 자동 실행
│  (→ main)       │     + 리뷰 2명
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   main 머지     │  → 스테이징 자동 배포
│                 │     (deploy-staging.yml)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  스테이징 검증  │  → QA 테스트
│                 │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  태그 생성      │  git tag v1.0.0
│  (v*.*.*)       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 프로덕션 배포   │  → 승인 대기 (30분)
│ (deploy-prod)   │     + 리뷰어 2명
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Health Check   │  → Slack 알림
│                 │
└─────────────────┘
```

---

## 🔍 주요 명령어

### CI 워크플로우

```bash
# 백엔드 CI 트리거
git add backend/
git commit -m "feat: add feature"
git push origin feature/my-feature

# 프론트엔드 CI 트리거
git add frontend/
git commit -m "feat: add UI component"
git push origin feature/my-feature
```

### 배포 워크플로우

```bash
# 개발 서버 배포 (자동)
git checkout develop
git merge feature/my-feature
git push origin develop

# 스테이징 배포 (자동)
git checkout main
git merge develop
git push origin main

# 프로덕션 배포 (태그)
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
# → GitHub Actions 탭에서 승인 필요
```

### 수동 워크플로우 실행

```bash
# GitHub 웹사이트에서:
# Actions 탭 → 원하는 워크플로우 선택 → Run workflow
```

### 롤백

```bash
# GitHub Actions 탭에서:
# Rollback Deployment 워크플로우 선택
# → Run workflow
# → Environment: production
# → Version: v1.0.0 (이전 버전)
# → Services: all
# → Reason: "Critical bug in v1.0.1"
```

---

## 🧪 테스트 체크리스트

### CI 테스트
- [ ] 백엔드 코드 변경 시 ci-backend 실행 확인
- [ ] 프론트엔드 코드 변경 시 ci-frontend 실행 확인
- [ ] PR 생성 시 CI 자동 실행 확인
- [ ] 테스트 실패 시 PR 머지 차단 확인

### 배포 테스트
- [ ] develop 브랜치 푸시 시 자동 배포 확인
- [ ] Docker 이미지 GHCR에 업로드 확인
- [ ] Health Check 통과 확인
- [ ] Slack 알림 수신 확인

### 보안 스캔 테스트
- [ ] 주간 스케줄 실행 확인
- [ ] Security 탭에서 결과 확인
- [ ] CRITICAL/HIGH 취약점 처리

### 롤백 테스트
- [ ] 이전 버전으로 롤백 성공
- [ ] Health Check 통과
- [ ] 서비스 정상 동작 확인

---

## 📈 모니터링

### GitHub Actions 대시보드
```
https://github.com/<username>/Stock-Simulator/actions
```

### 실행 상태 확인
- ✅ Success: 모든 단계 성공
- ⏸️ In Progress: 실행 중
- ❌ Failed: 실행 실패
- ⏭️ Skipped: 건너뜀
- 🔴 Cancelled: 취소됨

### 로그 확인
```
Actions 탭 → 워크플로우 선택 → Run 선택 → Job 선택 → Step 로그 확인
```

### Artifacts 다운로드
```
Actions 탭 → Run 선택 → Artifacts 섹션
- test-results
- coverage-reports
- owasp-dependency-check-report
- npm-audit-report
```

---

## ⚠️ 주의사항

### 1. Secrets 관리
- ❌ 절대 코드에 하드코딩 금지
- ✅ GitHub Secrets에만 저장
- ✅ 주기적으로 로테이션

### 2. 프로덕션 배포
- ❌ 직접 main 브랜치에 푸시 금지
- ✅ 반드시 PR + 리뷰 거쳐서 머지
- ✅ 스테이징 검증 후 태그 생성

### 3. 비용 관리
- GitHub Actions는 Public 리포지토리에서 무료
- Private 리포지토리는 월 2,000분 무료 (그 후 유료)
- Self-hosted runners 고려 가능

### 4. 보안
- CodeQL 스캔 결과 확인 필수
- Trivy 취약점 즉시 패치
- Dependabot 알림 활성화 권장

---

## 🔗 참고 링크

- [상세 가이드](./GitHub_Actions_가이드.md)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [Trivy](https://github.com/aquasecurity/trivy)

---

**작성일**: 2026년 1월 28일  
**버전**: 1.0  
**상태**: ✅ 완료  
**업데이트**: season-service 제거 반영
