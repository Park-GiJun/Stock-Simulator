# GitHub 설정 완벽 가이드 (처음부터 끝까지)

> **처음 GitHub Actions를 설정하는 분들을 위한 단계별 가이드**

---

## 🎯 목표

이 가이드를 따라하면:
- ✅ GitHub Actions가 정상 작동
- ✅ 자동 CI/CD 파이프라인 구축
- ✅ 개발/스테이징/프로덕션 환경 자동 배포

---

## 📋 사전 준비물

### 1. 필요한 서버
- **개발 서버** (선택): 테스트용 (VPS, EC2 등)
- **스테이징 서버** (선택): QA용
- **프로덕션 서버** (선택): 실 서비스용

> 💡 **팁**: 처음에는 개발 서버 1대만 준비해도 됩니다!

### 2. 서버 접속 정보
각 서버마다 필요:
- IP 주소 (예: `123.45.67.89`)
- SSH 사용자명 (예: `ubuntu`, `ec2-user`)
- SSH 포트 (기본: `22`)

---

## 🚀 Step 1: GitHub 기본 설정

### 1-1. Workflow Permissions 설정

**경로**: `Settings` → `Actions` → `General`

![image](https://github.com/user-attachments/assets/...)

**설정 내용**:
```
Workflow permissions
  ● Read and write permissions  ← 이거 선택
  ○ Read repository contents and packages permissions

□ Allow GitHub Actions to create and approve pull requests  ← 체크
```

**스크린샷으로 설명**:
1. 리포지토리 페이지에서 `Settings` 클릭
2. 왼쪽 메뉴에서 `Actions` → `General` 클릭
3. 맨 아래 "Workflow permissions" 섹션 찾기
4. 위 설정대로 변경
5. `Save` 버튼 클릭

---

## 🔐 Step 2: SSH 키 생성 및 등록

### 2-1. 로컬 컴퓨터에서 SSH 키 생성

**Windows (PowerShell 또는 Git Bash)**:
```bash
# 1. .ssh 폴더로 이동 (없으면 생성됨)
cd ~

# 2. SSH 키 생성
ssh-keygen -t ed25519 -C "github-actions@stocksimulator" -f ~/.ssh/github-actions

# 프롬프트 나올 때 Enter 3번 (비밀번호 없이)
```

**macOS/Linux**:
```bash
# 1. SSH 키 생성
ssh-keygen -t ed25519 -C "github-actions@stocksimulator" -f ~/.ssh/github-actions

# 프롬프트 나올 때 Enter 3번 (비밀번호 없이)
```

**생성된 파일**:
- `~/.ssh/github-actions` - 🔑 **프라이빗 키** (GitHub Secrets에 등록)
- `~/.ssh/github-actions.pub` - 🔓 **공개키** (서버에 등록)

### 2-2. 서버에 공개키 등록

**방법 1: ssh-copy-id 사용 (가장 간단)**
```bash
# 개발 서버
ssh-copy-id -i ~/.ssh/github-actions.pub ubuntu@123.45.67.89

# 비밀번호 입력하면 완료!
```

**방법 2: 수동 등록**
```bash
# 1. 공개키 내용 복사
cat ~/.ssh/github-actions.pub
# 출력된 내용 전체 복사 (ssh-ed25519 AAA...로 시작)

# 2. 서버 접속
ssh ubuntu@123.45.67.89

# 3. authorized_keys에 추가
mkdir -p ~/.ssh
echo "복사한_공개키_내용" >> ~/.ssh/authorized_keys
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

# 4. 로그아웃
exit
```

### 2-3. SSH 연결 테스트

```bash
# 새로 생성한 키로 접속 테스트
ssh -i ~/.ssh/github-actions ubuntu@123.45.67.89

# 비밀번호 없이 접속되면 성공! ✅
```

---

## 🔒 Step 3: GitHub Secrets 설정

### 3-1. SSH Private Key 등록

**경로**: `Settings` → `Secrets and variables` → `Actions` → `Secrets`

1. **`New repository secret` 버튼 클릭**

2. **SSH_PRIVATE_KEY 등록**
   ```
   Name: SSH_PRIVATE_KEY
   
   Secret: (아래 명령어로 복사한 내용 붙여넣기)
   ```
   
   **Secret 값 얻기**:
   ```bash
   # Windows (PowerShell)
   Get-Content ~/.ssh/github-actions | Set-Clipboard
   # 클립보드에 복사됨
   
   # macOS
   cat ~/.ssh/github-actions | pbcopy
   # 클립보드에 복사됨
   
   # Linux
   cat ~/.ssh/github-actions
   # 출력된 내용 수동 복사
   ```
   
   **복사할 내용 예시**:
   ```
   -----BEGIN OPENSSH PRIVATE KEY-----
   b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
   ... (여러 줄)
   -----END OPENSSH PRIVATE KEY-----
   ```
   
   > ⚠️ **주의**: `-----BEGIN`부터 `-----END`까지 전체를 복사해야 합니다!

3. **`Add secret` 버튼 클릭**

### 3-2. SERVER_USER 등록

1. **`New repository secret` 버튼 클릭**

2. **SERVER_USER 등록**
   ```
   Name: SERVER_USER
   Secret: ubuntu
   ```
   (서버 SSH 사용자명 입력)

3. **`Add secret` 버튼 클릭**

### 3-3. (선택) SLACK_WEBHOOK_URL 등록

Slack 알림을 받고 싶다면:

1. **Slack Incoming Webhook URL 생성**
   - Slack Workspace → Apps → Incoming Webhooks 검색
   - Add to Slack → 채널 선택 → Webhook URL 복사

2. **GitHub Secret 등록**
   ```
   Name: SLACK_WEBHOOK_URL
   Secret: https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX
   ```

---

## 🌍 Step 4: Environments 생성

### 4-1. Development Environment

**경로**: `Settings` → `Environments` → `New environment`

1. **Environment name**: `development` 입력 → `Configure environment`

2. **Environment secrets 추가**
   - `Add secret` 클릭
   - Name: `DEV_SERVER_HOST`
   - Secret: `123.45.67.89` (개발 서버 IP)
   - `Add secret` 클릭

3. **(선택) Protection rules 설정**
   ```
   Required reviewers: (비워둠 - 자동 배포)
   Wait timer: (비워둠)
   ```

4. **Environment variables 추가**
   - `Add variable` 클릭
   - Name: `DEV_URL`
   - Value: `http://dev.stocksimulator.com` (개발 서버 URL)
   - `Add variable` 클릭
   
   - `Add variable` 클릭
   - Name: `DEV_API_URL`
   - Value: `http://123.45.67.89:9832` (개발 서버 API URL)
   - `Add variable` 클릭

### 4-2. Staging Environment (선택)

스테이징 서버가 있다면:

1. **Environment name**: `staging` 입력 → `Configure environment`

2. **Environment secrets**
   - `STAGING_SERVER_HOST`: 스테이징 서버 IP

3. **Protection rules**
   ```
   Required reviewers: 1 ← 리뷰어 1명 추가
   ```

4. **Environment variables**
   - `STAGING_URL`: 스테이징 서버 URL
   - `STAGING_API_URL`: 스테이징 API URL

### 4-3. Production Environment (선택)

프로덕션 서버가 있다면:

1. **Environment name**: `production` 입력 → `Configure environment`

2. **Environment secrets**
   - `PROD_SERVER_HOST`: 프로덕션 서버 IP

3. **Protection rules** (중요!)
   ```
   Required reviewers: 2 ← 리뷰어 2명 추가
   Wait timer: 30 ← 30분 대기
   Deployment branches: Selected branches → main만 허용
   ```

4. **Environment variables**
   - `PROD_URL`: 프로덕션 서버 URL
   - `PROD_API_URL`: 프로덕션 API URL

---

## 🖥️ Step 5: 서버 준비

각 서버(개발/스테이징/프로덕션)에서 실행:

### 5-1. Docker 설치

```bash
# 서버 접속
ssh ubuntu@123.45.67.89

# Docker 설치 스크립트 다운로드 및 실행
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 로그아웃 후 재접속 (그룹 변경 적용)
exit
ssh ubuntu@123.45.67.89

# Docker 정상 작동 확인
docker ps
# 에러 없이 빈 목록이 나오면 성공!
```

### 5-2. 프로젝트 클론

```bash
# 개발 서버용
git clone https://github.com/YOUR_USERNAME/Stock-Simulator.git ~/Stock-Simulator-dev
cd ~/Stock-Simulator-dev
git checkout develop

# 스테이징/프로덕션 서버용
git clone https://github.com/YOUR_USERNAME/Stock-Simulator.git ~/Stock-Simulator
cd ~/Stock-Simulator
git checkout main
```

### 5-3. .env 파일 설정

```bash
# .env 파일 생성
cd ~/Stock-Simulator-dev  # 또는 ~/Stock-Simulator
nano .env
```

**.env 파일 내용**:
```env
# Infrastructure hosts (모두 서버 IP 또는 localhost)
EUREKA_HOST=172.30.1.1
POSTGRES_HOST=172.30.1.1
REDIS_HOST=172.30.1.1
MONGO_HOST=172.30.1.1
KAFKA_HOST=172.30.1.1
ELASTICSEARCH_HOST=172.30.1.1

# Credentials (원하는 값으로 변경)
POSTGRES_USER=stocksim
POSTGRES_PASSWORD=stocksim123
POSTGRES_DB=stocksimulator
REDIS_PASSWORD=stocksim123
MONGO_USER=stocksim
MONGO_PASSWORD=stocksim123

# Spring profile
SPRING_PROFILES_ACTIVE=docker
```

저장: `Ctrl + X` → `Y` → `Enter`

### 5-4. Docker 네트워크 확인

```bash
# .env 파일의 IP 주소 확인
docker network inspect bridge | grep Gateway
# "Gateway": "172.30.1.1" 같은 값이 나옴

# .env의 모든 *_HOST를 이 IP로 수정
nano .env
# 모든 172.30.1.X를 실제 Gateway IP로 변경
```

### 5-5. 초기 실행 테스트 (선택)

```bash
# 모든 인프라 서비스 실행
docker-compose --profile infra up -d

# 정상 실행 확인
docker-compose --profile infra ps

# 로그 확인
docker-compose --profile infra logs -f

# 종료
docker-compose --profile infra down
```

---

## ✅ Step 6: 첫 배포 테스트

### 6-1. 코드 푸시로 자동 배포 트리거

```bash
# 로컬 컴퓨터에서

# 1. develop 브랜치로 이동
git checkout develop

# 2. 빈 커밋으로 배포 트리거
git commit --allow-empty -m "test: trigger first deploy"

# 3. GitHub에 푸시
git push origin develop
```

### 6-2. GitHub Actions 확인

1. **GitHub 리포지토리 페이지 접속**
2. **`Actions` 탭 클릭**
3. **"Deploy to Development" 워크플로우 클릭**
4. **가장 최근 실행 클릭**
5. **각 Job 상태 확인**

**실행 단계**:
```
Build & Push Docker Images (9개 병렬)
  ├─ eureka-server      ✅
  ├─ api-gateway        ✅
  ├─ user-service       ✅
  ├─ stock-service      ✅
  ├─ trading-service    ✅
  ├─ event-service      ✅
  ├─ scheduler-service  ✅
  ├─ news-service       ✅
  └─ frontend           ✅
       ↓
Deploy to Dev Server    ✅
       ↓
Health Check            ✅
       ↓
Notify (Slack)          ✅
```

### 6-3. 서버에서 확인

```bash
# 서버 접속
ssh ubuntu@123.45.67.89

# 프로젝트 디렉토리 이동
cd ~/Stock-Simulator-dev

# 컨테이너 상태 확인
docker-compose --profile all ps

# 서비스별 상태 확인
docker-compose --profile all ps | grep Up
# 모든 서비스가 Up이면 성공!

# API Gateway 헬스체크
curl http://localhost:9832/actuator/health
# {"status":"UP"} 응답이 나오면 성공!

# Eureka 대시보드 확인
curl http://localhost:8761
# HTML 응답이 나오면 성공!

# Frontend 확인
curl http://localhost:8080
# HTML 응답이 나오면 성공!
```

### 6-4. 로그 확인

```bash
# 전체 로그
docker-compose --profile all logs

# 특정 서비스 로그 (실시간)
docker logs stockSimulator-api-gateway -f

# 최근 100줄만 보기
docker logs stockSimulator-api-gateway --tail 100
```

---

## 🐛 트러블슈팅

### 문제 1: SSH Permission Denied

**증상**:
```
Error: Permission denied (publickey)
```

**해결**:
```bash
# 1. 서버에서 권한 확인
ssh ubuntu@123.45.67.89
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

# 2. authorized_keys 내용 확인
cat ~/.ssh/authorized_keys
# 공개키가 제대로 들어있는지 확인

# 3. 로컬에서 재등록
ssh-copy-id -i ~/.ssh/github-actions.pub ubuntu@123.45.67.89
```

### 문제 2: Docker Login Failed

**증상**:
```
Error: denied: permission_denied
```

**해결**:
1. Settings → Actions → General
2. Workflow permissions를 "Read and write permissions"로 변경
3. Save 클릭
4. 워크플로우 재실행

### 문제 3: Container Failed to Start

**증상**:
```
Error: Container exited with code 1
```

**해결**:
```bash
# 서버에서 로그 확인
docker logs stockSimulator-api-gateway

# 환경 변수 확인
docker-compose --profile all config

# .env 파일 확인
cat .env

# 네트워크 IP 확인
docker network inspect bridge | grep Gateway

# .env의 모든 *_HOST를 올바른 IP로 수정
nano .env
```

### 문제 4: Health Check Failed

**증상**:
```
❌ API Gateway: DOWN
```

**해결**:
```bash
# 1. 컨테이너 상태 확인
docker-compose --profile all ps

# 2. 재시작이 반복되는 서비스 찾기
docker-compose --profile all ps | grep Restarting

# 3. 해당 서비스 로그 확인
docker logs stockSimulator-XXXXX --tail 50

# 4. 의존성 서비스 확인 (PostgreSQL, MongoDB, Redis, Kafka)
docker logs stockSimulator-postgres --tail 50
docker logs stockSimulator-mongo --tail 50
docker logs stockSimulator-redis --tail 50
docker logs stockSimulator-kafka --tail 50

# 5. 전체 재시작
docker-compose --profile all down
docker-compose --profile all up -d

# 6. 30초 대기 후 다시 확인
sleep 30
curl http://localhost:9832/actuator/health
```

### 문제 5: Out of Disk Space

**증상**:
```
Error: No space left on device
```

**해결**:
```bash
# 디스크 사용량 확인
df -h

# 사용하지 않는 Docker 리소스 정리
docker system prune -a --volumes

# 정말로 정리할까요? y 입력

# 다시 확인
df -h
```

---

## 📊 설정 완료 체크리스트

### GitHub 설정
- [ ] Workflow permissions: Read and write ✅
- [ ] Create and approve PR 허용 ✅

### Secrets
- [ ] `SSH_PRIVATE_KEY` 등록 ✅
- [ ] `SERVER_USER` 등록 ✅
- [ ] `SLACK_WEBHOOK_URL` 등록 (선택) 🔲

### Environments
- [ ] `development` environment 생성 ✅
  - [ ] `DEV_SERVER_HOST` 등록 ✅
  - [ ] `DEV_URL` 등록 ✅
  - [ ] `DEV_API_URL` 등록 ✅
- [ ] `staging` environment 생성 (선택) 🔲
- [ ] `production` environment 생성 (선택) 🔲

### 서버 준비
- [ ] Docker 설치 ✅
- [ ] SSH 공개키 등록 ✅
- [ ] 프로젝트 클론 ✅
- [ ] .env 파일 설정 ✅

### 배포 테스트
- [ ] develop 브랜치 푸시 ✅
- [ ] GitHub Actions 성공 ✅
- [ ] Health Check 통과 ✅
- [ ] 서비스 정상 동작 ✅

---

## 🎉 완료!

모든 체크리스트를 완료했다면:

✅ **GitHub Actions가 정상 작동합니다!**

이제부터:
- `develop` 브랜치에 푸시하면 → 개발 서버 자동 배포
- `main` 브랜치에 푸시하면 → 스테이징/프로덕션 배포
- PR 생성하면 → 자동 CI 테스트

---

## 📚 다음 단계

1. **[GitHub Actions 가이드](./GitHub_Actions_가이드.md)** - 상세 설명
2. **[빠른 시작 가이드](./GitHub_Actions_빠른시작.md)** - 명령어 모음
3. 실제 개발 시작! 🚀

---

## 💬 도움이 필요하면?

1. GitHub Issues에 질문 올리기
2. 로그 첨부해서 문의
3. Discord/Slack 커뮤니티 활용

---

**작성일**: 2026년 1월 28일  
**버전**: 1.0  
**난이도**: 초급  
**예상 소요 시간**: 30~60분
