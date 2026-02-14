# TeamCity CI/CD Setup Guide

## 1. TeamCity 설치 및 시작

```bash
cd ~/Stock-Simulator/teamcity
chmod +x setup.sh
./setup.sh
```

## 2. TeamCity 초기 설정

1. 브라우저에서 `http://172.30.1.79:8111` 접속
2. 라이선스 동의
3. 데이터베이스 선택:
   - **Internal (HSQLDB)**: 빠른 시작 (개발/테스트용)
   - **PostgreSQL**: 운영환경 권장 (stockSimulator-postgres 사용 가능)
4. Super User 토큰 입력 (터미널에 출력된 토큰 또는 아래 명령어로 확인):
   ```bash
   docker exec stockSimulator-teamcity-server cat /data/teamcity_server/datadir/system/token.txt
   ```
5. Admin 계정 생성

## 3. VCS Root 설정 (GitHub 연동)

Administration > VCS Roots > Create VCS Root

- **Type**: Git
- **VCS root name**: Stock-Simulator
- **Fetch URL**: `https://github.com/Park-GiJun/Stock-Simulator.git`
- **Default branch**: `refs/heads/master`
- **Authentication method**: Password/Token
  - Username: `Park-GiJun`
  - Password: GitHub Personal Access Token

### GitHub Token 생성:
1. GitHub > Settings > Developer settings > Personal access tokens > Tokens (classic)
2. Generate new token (classic)
3. Scopes:
   - [x] repo (전체)
   - [x] write:packages
   - [x] read:packages
   - [x] delete:packages

## 4. Versioned Settings (Kotlin DSL)

Administration > Project Settings > Versioned Settings

- **Synchronization enabled**: Yes
- **VCS Root**: Stock-Simulator (위에서 생성한 VCS Root)
- **Settings format**: Kotlin
- **Settings directory**: `.teamcity`
- **Apply changes from VCS**: Yes

이렇게 설정하면 `.teamcity/settings.kts` 파일에서 파이프라인 설정을 자동으로 로드합니다.

## 5. GHCR 인증 설정

Build Configuration > Parameters 에서 다음 파라미터 설정:

| Parameter | Type | Value |
|-----------|------|-------|
| `env.DOCKER_USER` | Password | GitHub 사용자명 |
| `env.DOCKER_PASSWORD` | Password | GitHub Personal Access Token |

## 6. GitHub Webhook 설정

GitHub Repository > Settings > Webhooks > Add webhook

- **Payload URL**: `http://172.30.1.79:8111/app/rest/vcs/checkForChanges`
- **Content type**: application/json
- **Events**: Just the push event
- **Active**: Yes

## 7. 첫 빌드 실행

Projects > StockSimulatorDeploy > Run

- version: v1.4.2
- environment: production
- buildTarget: all
- **Run Build**

## 8. Agent 확인

Administration > Agents

- `stockSimulator-teamcity-agent`가 Connected 상태인지 확인
- Agent에 Docker가 사용 가능한지 확인 (Agent > Agent Parameters에서 `docker.version` 확인)

## 트러블슈팅

### Agent가 연결되지 않음
```bash
# Agent 로그 확인
docker logs stockSimulator-teamcity-agent --tail 50

# Agent 재시작
cd ~/Stock-Simulator/teamcity
docker-compose restart teamcity-agent
```

### Docker 빌드 실패
```bash
# Agent 컨테이너에서 Docker 확인
docker exec stockSimulator-teamcity-agent docker version
```

### Gradle 빌드 실패
```bash
# Agent 워크스페이스에서 직접 빌드 테스트
docker exec -it stockSimulator-teamcity-agent bash
cd /workspace
chmod +x gradlew
./gradlew build -x test
```

### 이미지 push 실패
- Parameters에서 `env.DOCKER_USER`, `env.DOCKER_PASSWORD`가 올바른지 확인
- GitHub Token이 packages write 권한이 있는지 확인

## 유용한 명령어

```bash
# TeamCity 로그 확인
docker logs stockSimulator-teamcity-server -f

# TeamCity 재시작
cd ~/Stock-Simulator/teamcity
docker-compose restart

# TeamCity 중지
docker-compose down

# TeamCity 완전 삭제 (볼륨 포함)
docker-compose down -v
```
