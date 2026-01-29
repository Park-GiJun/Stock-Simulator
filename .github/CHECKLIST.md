# GitHub Actions 설정 체크리스트

## ✅ 완료된 작업

- [x] CI 워크플로우 생성 (`.github/workflows/ci.yml`)
- [x] Production 배포 워크플로우 생성 (`.github/workflows/deploy-production.yml`)
- [x] Rollback 워크플로우 생성 (`.github/workflows/rollback.yml`)
- [x] Security Scan 워크플로우 유지 (`.github/workflows/security-scan.yml`)
- [x] 기존 dev/staging 워크플로우 제거
- [x] 상세 설정 가이드 작성 (`.github/SETUP_GUIDE.md`)
- [x] 빠른 시작 가이드 작성 (`.github/QUICKSTART.md`)
- [x] 워크플로우 가이드 작성 (`.github/README.md`)
- [x] 루트 CICD 요약 문서 작성 (`CICD.md`)

## 📋 배포 전 필수 설정

### 1. GitHub Secrets 설정
- [ ] Repository → Settings → Secrets and variables → Actions
- [ ] `SSH_PRIVATE_KEY` 생성 및 등록
  ```bash
  # 서버에서 실행
  ssh gijunpark@172.30.1.79
  ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions
  cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys
  cat ~/.ssh/github_actions  # 내용 복사하여 GitHub에 등록
  ```

### 2. GitHub Environments 설정
- [ ] Repository → Settings → Environments
- [ ] `production` 생성 (Required reviewers 설정)
- [ ] `production-approval` 생성 (Required reviewers 설정)
- [ ] `production-rollback` 생성 (Required reviewers 설정)

### 3. 서버 환경 확인
- [ ] Docker 및 Docker Compose 설치 확인
  ```bash
  ssh gijunpark@172.30.1.79
  docker --version
  docker-compose --version
  ```
- [ ] 프로젝트 디렉토리 확인
  ```bash
  cd ~/Stock-Simulator
  ls -la
  ```
- [ ] `.env` 파일 설정 확인
  ```bash
  cat .env | grep -E "HOST|PASSWORD|USER"
  ```
- [ ] 백업 디렉토리 생성
  ```bash
  mkdir -p ~/production-backups
  mkdir -p ~/rollback-backups
  ```

### 4. GitHub Container Registry 권한 확인
- [ ] Repository → Settings → Actions → General
- [ ] Workflow permissions → "Read and write permissions" 선택
- [ ] "Allow GitHub Actions to create and approve pull requests" 체크

## 🚀 첫 배포 테스트

### 1단계: CI 테스트
```bash
# 아무 브랜치에 push하여 CI 테스트
git add .
git commit -m "chore: test CI"
git push
# GitHub Actions에서 CI 성공 확인
```

### 2단계: Production 배포
```bash
# 첫 배포 태그 생성
git tag -a v1.0.0 -m "First production release"
git push origin v1.0.0

# GitHub Actions 확인
# 1. build-and-push 완료
# 2. manual-approval에서 승인
# 3. 배포 진행
# 4. 헬스체크 확인
```

### 3단계: 배포 확인
```bash
# 외부 접근 테스트
curl https://gijun.net
curl https://api.gijun.net/actuator/health

# 서버에서 확인
ssh gijunpark@172.30.1.79
docker-compose --profile all ps
docker logs stockSimulator-frontend --tail=50
docker logs stockSimulator-api-gateway --tail=50
```

### 4단계: 모니터링 확인
- [ ] Grafana: http://172.30.1.79:3001 (admin / stocksim123)
- [ ] Prometheus: http://172.30.1.79:9091
- [ ] Eureka: http://172.30.1.79:8761

## 📝 선택 설정

### Slack 알림 (선택사항)
- [ ] Slack Workspace에서 Incoming Webhook 생성
- [ ] GitHub Secret `SLACK_WEBHOOK_URL` 등록

## 🔍 확인 사항

### 워크플로우 파일 확인
```bash
ls -la .github/workflows/
# ci.yml
# deploy-production.yml
# rollback.yml
# security-scan.yml
```

### 문서 확인
```bash
ls -la .github/
# README.md
# QUICKSTART.md
# SETUP_GUIDE.md

# 루트에 CICD.md 확인
cat CICD.md
```

## ⚠️ 주의사항

1. **수동 승인 필수**: Production 배포는 반드시 수동 승인이 필요합니다
2. **백업 확인**: 배포 전 자동으로 백업이 생성됩니다 (`~/production-backups/`)
3. **롤백 준비**: 문제 발생시 즉시 롤백할 수 있도록 준비하세요
4. **모니터링**: 배포 후 Grafana에서 서비스 상태를 모니터링하세요

## 📞 문제 발생시

### GitHub Actions 로그 확인
- Repository → Actions → 해당 워크플로우 클릭
- 각 Job의 로그에서 에러 확인

### 서버 로그 확인
```bash
ssh gijunpark@172.30.1.79
docker-compose --profile all logs --tail=100
```

### 긴급 롤백
```bash
# GitHub Actions에서 Rollback 워크플로우 실행
# 또는 서버에서 직접:
ssh gijunpark@172.30.1.79
cd ~/Stock-Simulator
BACKUP=$(ls -t ~/production-backups/ | head -1)
cp ~/production-backups/$BACKUP/.env .env
docker-compose --profile all down
docker-compose --profile all up -d
```

## 🎯 다음 단계

1. [ ] 모든 설정 완료 후 첫 배포 테스트
2. [ ] 배포 성공 확인 후 팀원들에게 배포 프로세스 공유
3. [ ] 정기적인 보안 스캔 결과 확인 (주 1회 자동)
4. [ ] 배포 문서를 팀 위키에 추가

---

**참고 문서:**
- [빠른 시작](.github/QUICKSTART.md)
- [상세 가이드](.github/SETUP_GUIDE.md)
- [워크플로우 가이드](.github/README.md)
