# Git Branch Strategy

## 개요

Stock-Simulator 프로젝트는 **Git Flow 기반의 Feature Branch 전략**을 사용합니다.  
각 기능은 독립적인 브랜치에서 개발되며, 코드 리뷰를 거쳐 develop 브랜치로 통합됩니다.

---

## 브랜치 구조

```
master (main)
  ↓
develop
  ↓
feature/* (각 기능별 브랜치)
hotfix/* (긴급 수정)
release/* (배포 준비)
```

### 주요 브랜치

#### `master`
- **용도**: 프로덕션 배포용 안정 브랜치
- **보호**: 직접 커밋 금지, PR을 통한 Merge만 허용
- **태그**: 각 배포 버전에 태그 (`v1.0.0`, `v1.1.0` 등)

#### `develop`
- **용도**: 개발 통합 브랜치
- **기능**: 모든 feature 브랜치가 여기로 merge
- **배포**: 충분히 테스트 후 master로 merge

---

## Feature 브랜치 분류

### Infrastructure Features (`feature/infra-*`)

| Branch | 설명 | 담당 |
|--------|------|------|
| `feature/infra-monitoring` | Prometheus/Grafana 모니터링 | Infrastructure |
| `feature/infra-docker-setup` | Docker Compose 인프라 | Infrastructure |
| `feature/infra-cicd` | CI/CD 파이프라인 | Infrastructure |

### Backend Service Features (`feature/service-*`)

| Branch | Port | 설명 |
|--------|------|------|
| `feature/service-eureka` | 8761 | Eureka 서비스 디스커버리 |
| `feature/service-gateway` | 9832 | API Gateway |
| `feature/service-user` | 8081 | User Service (인증, 사용자 관리) |
| `feature/service-stock` | 8082 | Stock Service (주식 정보, 가격 업데이트) |
| `feature/service-trading` | 8083 | Trading Service (주문, 체결, NPC 거래) |
| `feature/service-event` | 8084 | Event Service (게임 이벤트 생성) |
| `feature/service-scheduler` | 8085 | Scheduler Service (IPO, 상장폐지, NPC 생성) |
| `feature/service-news` | 8086 | News Service (AI 뉴스 생성) |

### Domain Feature Branches (`feature/*-*`)

#### Trading Domain
| Branch | 설명 |
|--------|------|
| `feature/trading-orderbook` | 주문장(Order Book) 시스템 |
| `feature/trading-matching` | 주문 체결 엔진 |
| `feature/trading-npc` | NPC 투자자 거래 시스템 |

#### Stock Domain
| Branch | 설명 |
|--------|------|
| `feature/stock-ipo` | IPO (신규 상장) 시스템 |
| `feature/stock-delisting` | 상장폐지 시스템 |
| `feature/price-mechanism` | 주가 변동 메커니즘 (이벤트 기반) |

#### Investor Domain
| Branch | 설명 |
|--------|------|
| `feature/investor-generation` | NPC 투자자 생성 시스템 |

#### Event Domain
| Branch | 설명 |
|--------|------|
| `feature/event-generation` | 게임 이벤트 생성 시스템 |
| `feature/event-impact` | 이벤트 영향 분석 시스템 |

### Frontend Features (`feature/frontend-*`)

| Branch | 설명 |
|--------|------|
| `feature/frontend-setup` | SvelteKit 기본 설정 |
| `feature/frontend-auth` | 로그인/회원가입 UI |
| `feature/frontend-market` | 시장 현황 페이지 |
| `feature/frontend-trading` | 거래 페이지 (주문, 체결) |
| `feature/frontend-portfolio` | 포트폴리오 페이지 |
| `feature/frontend-news` | 뉴스 피드 |
| `feature/frontend-realtime` | 실시간 WebSocket 연동 |
| `feature/frontend-mobile` | 모바일 UI (`/m/*` 라우트) |

### Integration Features (`feature/*-*`)

| Branch | 설명 |
|--------|------|
| `feature/kafka-events` | Kafka 이벤트 통합 (토픽별 Producer/Consumer) |
| `feature/redis-cache` | Redis 캐싱 전략 |
| `feature/db-replication` | PostgreSQL 읽기/쓰기 분리 |

---

## 브랜치 네이밍 규칙

### Feature 브랜치
```
feature/<category>-<name>
```

**예시:**
- `feature/service-user` - User Service 개발
- `feature/trading-orderbook` - 주문장 시스템
- `feature/frontend-auth` - 프론트엔드 인증 UI
- `feature/infra-monitoring` - 모니터링 인프라

### Hotfix 브랜치
```
hotfix/<issue-number>-<short-description>
```

**예시:**
- `hotfix/123-fix-login-error`
- `hotfix/456-kafka-connection-issue`

### Release 브랜치
```
release/v<version>
```

**예시:**
- `release/v1.0.0`
- `release/v1.1.0`

---

## 개발 워크플로우

### 1. Feature 개발 시작

```bash
# develop 브랜치 최신화
git checkout develop
git pull origin develop

# 새로운 feature 브랜치 생성
git checkout -b feature/your-feature-name

# 작업 진행...
```

### 2. 작업 및 커밋

```bash
# 변경사항 스테이징
git add .

# 커밋 (Commit Convention 준수)
git commit -m "feat: Add feature description"

# 원격 저장소에 푸시
git push -u origin feature/your-feature-name
```

### 3. Pull Request 생성

1. GitHub에서 PR 생성
2. **Base branch**: `develop`
3. **Compare branch**: `feature/your-feature-name`
4. PR 템플릿 작성:
   - 변경 내용 요약
   - 테스트 방법
   - 스크린샷 (UI 변경 시)
   - 관련 이슈 링크

### 4. Code Review

- 최소 1명 이상의 리뷰어 승인 필요
- CI/CD 테스트 통과 확인
- Conflict 해결

### 5. Merge

```bash
# Squash Merge 권장 (커밋 히스토리 정리)
# GitHub UI에서 "Squash and merge" 사용

# Merge 후 로컬 브랜치 정리
git checkout develop
git pull origin develop
git branch -d feature/your-feature-name
```

### 6. 원격 브랜치 정리

```bash
# GitHub에서 자동 삭제 설정 권장
# 또는 수동 삭제
git push origin --delete feature/your-feature-name
```

---

## Commit Convention

### 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type

| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat: Add user login API` |
| `fix` | 버그 수정 | `fix: Fix null pointer in trading service` |
| `docs` | 문서 변경 | `docs: Update README with setup instructions` |
| `style` | 코드 포맷팅 (기능 변경 없음) | `style: Format code with Prettier` |
| `refactor` | 리팩토링 | `refactor: Improve order matching algorithm` |
| `test` | 테스트 추가/수정 | `test: Add unit tests for stock service` |
| `chore` | 빌드/설정 변경 | `chore: Update Gradle dependencies` |
| `perf` | 성능 개선 | `perf: Optimize Redis cache queries` |

### 예시

```bash
# 간단한 커밋
git commit -m "feat: Add WebSocket endpoint for real-time prices"

# 상세 커밋
git commit -m "feat(trading): Implement order matching engine

- Add price priority and time priority logic
- Integrate with Redis for order book caching
- Add Kafka producer for trade execution events

Closes #123"
```

---

## Merge 전략

### Squash Merge (권장)

**장점:**
- 깔끔한 커밋 히스토리
- Feature 단위로 커밋 관리
- Revert 시 편리

**사용 시나리오:**
- Feature 브랜치 → develop
- Feature 브랜치 → feature 브랜치 (서브 기능)

### Merge Commit

**장점:**
- 전체 커밋 히스토리 보존
- 작업 과정 추적 가능

**사용 시나리오:**
- develop → master (릴리즈)
- hotfix → develop/master

---

## 브랜치 보호 규칙 (GitHub Settings)

### `master` 브랜치

- [x] Require pull request reviews before merging (1명 이상)
- [x] Require status checks to pass before merging
- [x] Require branches to be up to date before merging
- [x] Include administrators
- [x] Restrict who can push to matching branches

### `develop` 브랜치

- [x] Require pull request reviews before merging (1명 이상)
- [x] Require status checks to pass before merging
- [x] Require branches to be up to date before merging

---

## Release 프로세스

### 1. Release 브랜치 생성

```bash
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0
```

### 2. 버전 정보 업데이트

- `package.json` (Frontend)
- `build.gradle.kts` (Backend)
- `CHANGELOG.md` 작성

### 3. Release PR 생성

```bash
git push -u origin release/v1.0.0
```

- **Base**: `master`
- **Compare**: `release/v1.0.0`

### 4. Master로 Merge 후 태그

```bash
git checkout master
git pull origin master
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 5. Develop에 Merge Back

```bash
git checkout develop
git merge master
git push origin develop
```

---

## Hotfix 프로세스

### 1. Hotfix 브랜치 생성

```bash
git checkout master
git checkout -b hotfix/123-fix-critical-bug
```

### 2. 버그 수정 및 테스트

```bash
git commit -m "fix: Resolve critical bug in order matching"
git push -u origin hotfix/123-fix-critical-bug
```

### 3. Master & Develop에 Merge

```bash
# Master로 PR 생성 및 Merge
# Develop로 PR 생성 및 Merge
```

### 4. 태그 생성

```bash
git checkout master
git pull origin master
git tag -a v1.0.1 -m "Hotfix version 1.0.1"
git push origin v1.0.1
```

---

## 브랜치 라이프사이클

### Feature 브랜치

1. **생성**: develop에서 분기
2. **개발**: 독립적으로 작업
3. **PR**: develop으로 Pull Request
4. **Review**: 코드 리뷰 및 테스트
5. **Merge**: Squash merge로 통합
6. **삭제**: Merge 후 브랜치 삭제

### Release 브랜치

1. **생성**: develop에서 분기
2. **준비**: 버전 정보 업데이트, 최종 테스트
3. **Merge**: master로 merge
4. **태그**: 버전 태그 생성
5. **Backport**: develop으로 merge back
6. **삭제**: Merge 후 브랜치 삭제

### Hotfix 브랜치

1. **생성**: master에서 분기
2. **수정**: 긴급 버그 수정
3. **Merge**: master와 develop 모두에 merge
4. **태그**: 패치 버전 태그 생성
5. **삭제**: Merge 후 브랜치 삭제

---

## 현재 브랜치 현황

### 생성 완료 (24개)

#### Infrastructure (1)
- `feature/infra-monitoring`

#### Backend Services (8)
- `feature/service-eureka`
- `feature/service-gateway`
- `feature/service-user`
- `feature/service-stock`
- `feature/service-trading`
- `feature/service-event`
- `feature/service-scheduler`
- `feature/service-news`

#### Domain Features (8)
- `feature/trading-orderbook`
- `feature/trading-matching`
- `feature/trading-npc`
- `feature/price-mechanism`
- `feature/stock-ipo`
- `feature/stock-delisting`
- `feature/investor-generation`
- `feature/event-generation`

#### Frontend (4)
- `feature/frontend-setup`
- `feature/frontend-auth`
- `feature/frontend-trading`
- `feature/frontend-realtime`

#### Integration (2)
- `feature/kafka-events`
- `feature/redis-cache`

#### Legacy (1)
- `feature/user-service-hexagonal-structure` (→ `feature/service-user`로 이관 권장)

---

## Best Practices

### DO ✅

- Feature는 가능한 작게 나누기 (1~2주 내 완료 가능한 단위)
- 커밋 메시지 규칙 준수
- PR 전 로컬에서 충분히 테스트
- Conflict 발생 시 즉시 해결
- 코드 리뷰에 적극 참여
- 브랜치명은 명확하고 일관성 있게

### DON'T ❌

- master에 직접 커밋
- develop에 직접 커밋 (PR 없이)
- 오래된 브랜치 방치 (주기적으로 정리)
- 의미 없는 커밋 메시지 (`fix`, `update` 등)
- 대용량 파일 커밋 (`.gitignore` 활용)
- 민감 정보 커밋 (API Key, Password 등)

---

## 참고 자료

- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)

---

## 문의

브랜치 전략 관련 문의는 팀 리드에게 연락하세요.
