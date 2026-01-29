# ✅ GitHub Actions 설정 체크리스트

> **복사해서 메모장에 붙여넣고 하나씩 체크하세요!**

---

## 🎯 필수 설정 (개발 서버 1대)

```
□ Step 1: GitHub 기본 설정 (5분)
  └─ Settings → Actions → General
     └─ Workflow permissions: "Read and write permissions" 선택
     └─ "Allow GitHub Actions to create and approve pull requests" 체크
     └─ Save 클릭

□ Step 2: SSH 키 생성 (5분)
  └─ 로컬 컴퓨터에서:
     └─ ssh-keygen -t ed25519 -C "github-actions@stocksimulator" -f ~/.ssh/github-actions
     └─ Enter 3번 (비밀번호 없음)
     └─ 생성 완료 확인: ls ~/.ssh/github-actions*

□ Step 3: 서버에 공개키 등록 (5분)
  └─ ssh-copy-id -i ~/.ssh/github-actions.pub ubuntu@서버IP
  └─ 비밀번호 입력
  └─ 테스트: ssh -i ~/.ssh/github-actions ubuntu@서버IP
  └─ 비밀번호 없이 접속되면 성공!

□ Step 4: GitHub Secrets 등록 (10분)
  └─ Settings → Secrets and variables → Actions → Secrets
  
  ┌─ SSH_PRIVATE_KEY
  │  └─ cat ~/.ssh/github-actions (내용 전체 복사)
  │  └─ New repository secret
  │  └─ Name: SSH_PRIVATE_KEY
  │  └─ Secret: (복사한 내용 붙여넣기)
  │  └─ Add secret
  │
  └─ SERVER_USER
     └─ New repository secret
     └─ Name: SERVER_USER
     └─ Secret: ubuntu (또는 ec2-user)
     └─ Add secret

□ Step 5: Development Environment 생성 (10분)
  └─ Settings → Environments → New environment
  └─ Name: development
  └─ Configure environment
  
  ┌─ Environment secrets
  │  └─ Add secret
  │  └─ Name: DEV_SERVER_HOST
  │  └─ Secret: 123.45.67.89 (서버 IP)
  │  └─ Add secret
  │
  └─ Environment variables
     ├─ Add variable
     │  └─ Name: DEV_URL
     │  └─ Value: http://dev.stocksimulator.com
     │  └─ Add variable
     │
     └─ Add variable
        └─ Name: DEV_API_URL
        └─ Value: http://123.45.67.89:9832
        └─ Add variable

□ Step 6: 서버에 Docker 설치 (10분)
  └─ ssh ubuntu@서버IP
  └─ curl -fsSL https://get.docker.com -o get-docker.sh
  └─ sudo sh get-docker.sh
  └─ sudo usermod -aG docker $USER
  └─ exit
  └─ ssh ubuntu@서버IP (재접속)
  └─ docker ps (정상 작동 확인)

□ Step 7: 서버에 프로젝트 클론 (5분)
  └─ git clone https://github.com/YOUR_USERNAME/Stock-Simulator.git ~/Stock-Simulator-dev
  └─ cd ~/Stock-Simulator-dev
  └─ git checkout develop

□ Step 8: .env 파일 설정 (10분)
  └─ cd ~/Stock-Simulator-dev
  └─ nano .env
  └─ (아래 내용 복사해서 붙여넣기)
  
  ┌─────────────────────────────────────────────┐
  │ # .env 파일 내용                             │
  │ EUREKA_HOST=172.30.1.1                      │
  │ POSTGRES_HOST=172.30.1.1                    │
  │ REDIS_HOST=172.30.1.1                       │
  │ MONGO_HOST=172.30.1.1                       │
  │ KAFKA_HOST=172.30.1.1                       │
  │ ELASTICSEARCH_HOST=172.30.1.1               │
  │                                              │
  │ POSTGRES_USER=stocksim                      │
  │ POSTGRES_PASSWORD=stocksim123               │
  │ POSTGRES_DB=stocksimulator                  │
  │ REDIS_PASSWORD=stocksim123                  │
  │ MONGO_USER=stocksim                         │
  │ MONGO_PASSWORD=stocksim123                  │
  │                                              │
  │ SPRING_PROFILES_ACTIVE=docker               │
  └─────────────────────────────────────────────┘
  
  └─ 저장: Ctrl+X → Y → Enter

□ Step 9: 첫 배포 테스트 (15분)
  └─ 로컬 컴퓨터에서:
     └─ git checkout develop
     └─ git commit --allow-empty -m "test: first deploy"
     └─ git push origin develop
  
  └─ GitHub에서:
     └─ Actions 탭 클릭
     └─ "Deploy to Development" 워크플로우 확인
     └─ 모든 Job이 ✅ 초록색이면 성공!
  
  └─ 서버에서 확인:
     └─ ssh ubuntu@서버IP
     └─ cd ~/Stock-Simulator-dev
     └─ docker-compose --profile all ps
     └─ curl http://localhost:9832/actuator/health
     └─ {"status":"UP"} 나오면 성공! 🎉

□ Step 10: 완료! 🎉
  └─ 이제부터 develop 브랜치에 푸시하면 자동 배포됩니다!
```

---

## 📝 빠른 명령어 모음

### SSH 키 생성
```bash
ssh-keygen -t ed25519 -C "github-actions@stocksimulator" -f ~/.ssh/github-actions
```

### 공개키 서버 등록
```bash
ssh-copy-id -i ~/.ssh/github-actions.pub ubuntu@서버IP
```

### 프라이빗 키 복사 (Windows PowerShell)
```powershell
Get-Content ~/.ssh/github-actions | Set-Clipboard
```

### 프라이빗 키 복사 (macOS)
```bash
cat ~/.ssh/github-actions | pbcopy
```

### 프라이빗 키 복사 (Linux)
```bash
cat ~/.ssh/github-actions
# 출력된 내용 전체 복사
```

### Docker 설치
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

### 프로젝트 클론
```bash
git clone https://github.com/YOUR_USERNAME/Stock-Simulator.git ~/Stock-Simulator-dev
cd ~/Stock-Simulator-dev
git checkout develop
```

### 배포 트리거
```bash
git checkout develop
git commit --allow-empty -m "test: trigger deploy"
git push origin develop
```

### 서버 상태 확인
```bash
docker-compose --profile all ps
curl http://localhost:9832/actuator/health
curl http://localhost:8761
```

---

## 🚨 자주 발생하는 오류

### 1️⃣ SSH Permission Denied
```bash
# 해결 방법
ssh ubuntu@서버IP
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

### 2️⃣ Docker Login Failed
```
Settings → Actions → General 
→ "Read and write permissions" 선택
```

### 3️⃣ Container 시작 실패
```bash
# 로그 확인
docker logs stockSimulator-api-gateway

# .env 파일 IP 확인
docker network inspect bridge | grep Gateway
# 이 IP를 .env의 모든 *_HOST에 적용
```

### 4️⃣ Health Check 실패
```bash
# 전체 재시작
docker-compose --profile all down
docker-compose --profile all up -d
sleep 30
curl http://localhost:9832/actuator/health
```

---

## 🎯 최소 구성 (가장 빠른 방법)

개발 서버 1대만 있으면 충분합니다!

```
필수 Secrets (2개):
  ├─ SSH_PRIVATE_KEY
  └─ SERVER_USER

필수 Environment (1개):
  └─ development
     ├─ DEV_SERVER_HOST
     ├─ DEV_URL
     └─ DEV_API_URL

서버 준비:
  ├─ Docker 설치
  ├─ 프로젝트 클론
  └─ .env 파일 설정

완료! ✅
```

---

## 📊 진행 상황 추적

```
[단계 1] GitHub 기본 설정         [ ]  5분
[단계 2] SSH 키 생성              [ ]  5분
[단계 3] 서버에 공개키 등록        [ ]  5분
[단계 4] GitHub Secrets 등록      [ ] 10분
[단계 5] Environment 생성         [ ] 10분
[단계 6] Docker 설치              [ ] 10분
[단계 7] 프로젝트 클론            [ ]  5분
[단계 8] .env 설정               [ ] 10분
[단계 9] 첫 배포 테스트           [ ] 15분
                          ────────────
                          총 75분
```

---

## 🎓 다음 학습 자료

설정 완료 후:

1. **[GitHub Actions 가이드](./GitHub_Actions_가이드.md)**
   - 각 워크플로우 상세 설명
   - 고급 기능 활용법

2. **[빠른 시작 가이드](./GitHub_Actions_빠른시작.md)**
   - 자주 쓰는 명령어
   - 배포 시나리오

3. **[처음부터 설정하기](./GitHub_Actions_처음부터_설정하기.md)**
   - 스크린샷과 함께 하는 상세 가이드
   - 트러블슈팅

---

**이 체크리스트를 출력하거나 메모장에 복사해서 하나씩 체크하세요!**

**예상 소요 시간**: 1시간 ~ 1시간 30분  
**난이도**: 🟢 쉬움 (처음이어도 따라할 수 있음)  
**필요 사전 지식**: Git, SSH 기본 개념

---

**작성일**: 2026년 1월 28일  
**버전**: 1.0
