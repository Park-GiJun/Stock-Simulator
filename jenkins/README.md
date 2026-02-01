# Jenkins CI/CD Setup Guide

## 1. Jenkins 설치 및 시작

```bash
cd ~/Stock-Simulator/jenkins
chmod +x setup.sh
./setup.sh
```

## 2. Jenkins 초기 설정

1. 브라우저에서 `http://172.30.1.79:8090` 접속
2. 터미널에 출력된 초기 비밀번호 입력
3. "Install suggested plugins" 선택
4. Admin 계정 생성:
   - Username: admin
   - Password: (원하는 비밀번호)
   - Email: your-email@example.com

## 3. 필수 플러그인 설치

Dashboard → Manage Jenkins → Manage Plugins → Available

- Docker Pipeline
- Git
- Pipeline
- GitHub Integration
- Credentials Binding

## 4. GitHub Credentials 설정

Dashboard → Manage Jenkins → Manage Credentials → Global → Add Credentials

**Type:** Username with password
- Username: Park-GiJun
- Password: (GitHub Personal Access Token)
- ID: `github-token`
- Description: GitHub Container Registry Token

### GitHub Token 생성:
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token (classic)
3. Scopes:
   - [x] repo (전체)
   - [x] write:packages
   - [x] read:packages
   - [x] delete:packages

## 5. Pipeline Job 생성

Dashboard → New Item

**Name:** Stock-Simulator-Deploy
**Type:** Pipeline
**OK**

### Pipeline Configuration:

**Build Triggers:**
- [x] GitHub hook trigger for GITScm polling

**Pipeline:**
- Definition: Pipeline script from SCM
- SCM: Git
- Repository URL: `https://github.com/Park-GiJun/Stock-Simulator.git`
- Credentials: github-token
- Branch: */master
- Script Path: Jenkinsfile

**Save**

## 6. GitHub Webhook 설정 (선택사항)

GitHub Repository → Settings → Webhooks → Add webhook

- Payload URL: `http://172.30.1.79:8090/github-webhook/`
- Content type: application/json
- Events: Just the push event
- Active: ✓

## 7. 첫 배포 실행

Dashboard → Stock-Simulator-Deploy → Build with Parameters

- VERSION: v1.4.1
- ENVIRONMENT: production
- **Build**

## 8. 모니터링

- Build History에서 진행 상황 확인
- Console Output에서 상세 로그 확인

## 트러블슈팅

### Docker 권한 에러
```bash
sudo usermod -aG docker jenkins
docker restart jenkins
```

### Gradle 빌드 실패
```bash
# Jenkins 컨테이너 내부에서
docker exec -it jenkins bash
cd /workspace/Stock-Simulator
./gradlew clean build -x test
```

### 이미지 push 실패
- Credentials ID가 `github-token`인지 확인
- GitHub Token이 packages write 권한이 있는지 확인

## 유용한 명령어

```bash
# Jenkins 로그 확인
docker logs jenkins -f

# Jenkins 재시작
cd ~/Stock-Simulator/jenkins
docker-compose restart

# Jenkins 중지
docker-compose down

# Jenkins 완전 삭제 (볼륨 포함)
docker-compose down -v
```

## 자동화된 배포 플로우

1. 코드 변경 후 Git push
2. GitHub webhook이 Jenkins 트리거 (설정한 경우)
3. Jenkins가 자동으로:
   - 코드 체크아웃
   - Gradle 빌드
   - Docker 이미지 빌드 & 푸시
   - 프로덕션 배포
   - 헬스체크
4. Slack/Email 알림 (선택사항)

## 다음 단계

- [ ] Slack 알림 설정
- [ ] 자동 롤백 구현
- [ ] Blue-Green 배포 구현
- [ ] 테스트 자동화 추가
