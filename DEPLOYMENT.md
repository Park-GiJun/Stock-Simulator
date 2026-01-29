# 🚀 Stock-Simulator 배포 가이드

## 📋 목차
- [배포 프로세스](#-배포-프로세스)
- [버전 관리 규칙](#-버전-관리-규칙)
- [배포 방법](#-배포-방법)
- [롤백 방법](#-롤백-방법)
- [모니터링](#-모니터링)

---

## 🔄 배포 프로세스

```
코드 작업 → 커밋 & 푸시 → 태그 생성 → 자동 배포
```

---

## 📦 버전 관리 규칙

### Semantic Versioning (vX.Y.Z)

```
v1.0.0
│ │ │
│ │ └─ Patch: 버그 수정, 작은 변경
│ └─── Minor: 새 기능 추가 (하위 호환)
└───── Major: 호환성 없는 변경
```

### 버전 증가 규칙

| 변경 유형 | 버전 | 예시 |
|----------|------|------|
| 버그 수정 | Patch | v1.0.0 → v1.0.1 |
| 새 기능 추가 | Minor | v1.0.1 → v1.1.0 |
| 호환성 없는 변경 | Major | v1.1.0 → v2.0.0 |

### 예시

```bash
v1.0.0  # 첫 배포
v1.0.1  # 버그 수정
v1.1.0  # 새로운 기능 추가
v1.1.1  # 버그 수정
v2.0.0  # 대규모 변경 (DB 스키마 변경 등)
```

---

## 🚀 배포 방법

### 1. 코드 작업 및 커밋

```bash
# 코드 수정 후
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin master
```

### 2. 태그 생성 및 푸시

#### 방법 A: 명령어로 태그 생성 (권장)

```bash
# 현재 최신 버전 확인
git tag

# 새 버전 태그 생성
git tag -a v1.0.1 -m "Release v1.0.1: Bug fixes and improvements"

# 태그 푸시
git push origin v1.0.1
```

#### 방법 B: 한 줄로 태그 생성

```bash
git tag -a v1.0.1 -m "Release v1.0.1" && git push origin v1.0.1
```

### 3. GitHub Actions 확인

1. GitHub Repository → **Actions** 탭
2. **Deploy to Production** 워크플로우 실행 확인
3. 진행 단계:
   - ✅ Validate Deployment
   - ✅ Build & Push Images (5-10분)
   - ⏸️ **Manual Approval** (승인 필요)
   - ✅ Backup Production
   - ✅ Deploy to Production
   - ✅ Health Check
   - ✅ Smoke Tests
   - 🎉 Deployment Summary

### 4. 수동 승인

1. **Manual Approval** 단계에서 대기
2. **Review deployments** 버튼 클릭
3. 배포 내용 확인 후 **Approve and deploy** 클릭

### 5. 배포 완료 확인

```bash
# 프로덕션 URL 확인
curl https://gijun.net
curl https://api.gijun.net/actuator/health

# 또는 브라우저에서
# https://gijun.net
# https://api.gijun.net
```

---

## ⏪ 롤백 방법

### 언제 롤백하나요?

- 배포 후 심각한 버그 발견
- 서비스 장애 발생
- 성능 문제 발생

### 롤백 절차

1. **GitHub Actions에서 Rollback 실행**
   - Repository → **Actions** → **Rollback Deployment**
   - **Run workflow** 클릭

2. **입력 정보**
   ```
   version: v1.0.0  (롤백할 이전 버전)
   reason: Critical bug in user authentication
   ```

3. **Run workflow** 클릭

4. **Manual Approval** 단계에서 승인

5. **롤백 완료 확인**
   ```bash
   curl https://api.gijun.net/actuator/health
   ```

---

## 📊 모니터링

### GitHub Actions 로그

- URL: https://github.com/Park-GiJun/Stock-Simulator/actions
- 각 워크플로우의 상세 로그 확인 가능

### Grafana 대시보드

- URL: http://172.30.1.79:3001
- 로그인: `admin` / `stocksim123`
- Dashboard: **Stock Simulator - Services Overview**
- 모니터링 항목:
  - 서비스 상태 (UP/DOWN)
  - Request rate
  - Response time (p95)
  - JVM memory
  - CPU usage

### Prometheus

- URL: http://172.30.1.79:9091
- Targets: http://172.30.1.79:9091/targets
- 모든 서비스 메트릭 수집

### Eureka Dashboard

- URL: http://172.30.1.79:8761
- 등록된 마이크로서비스 확인

### 서버 로그 확인

```bash
# 서버 접속
ssh gijunpark@172.30.1.79
cd ~/Stock-Simulator

# 전체 컨테이너 상태
docker-compose --profile all ps

# 특정 서비스 로그
docker logs stockSimulator-frontend --tail=100 -f
docker logs stockSimulator-api-gateway --tail=100 -f
docker logs stockSimulator-user-service --tail=100 -f

# 에러 로그만 보기
docker logs stockSimulator-api-gateway 2>&1 | grep ERROR
```

---

## 🔍 배포 트러블슈팅

### 빌드 실패

```bash
# 로컬에서 빌드 테스트
cd backend
./gradlew clean build -x test

# 특정 서비스만
./gradlew :user-service:bootJar
```

### 배포 후 서비스 실패

```bash
# 서버에서 확인
ssh gijunpark@172.30.1.79
docker-compose --profile all ps
docker logs stockSimulator-<service-name>

# 서비스 재시작
docker-compose --profile all restart <service-name>
```

### 헬스체크 실패

```bash
# API Gateway 헬스체크
curl http://localhost:9832/actuator/health

# Eureka 등록 확인
curl http://localhost:8761/eureka/apps
```

---

## 📝 배포 체크리스트

### 배포 전

- [ ] 코드 리뷰 완료
- [ ] 로컬 테스트 완료
- [ ] CI 테스트 통과
- [ ] 배포 노트 작성 (태그 메시지)

### 배포 중

- [ ] GitHub Actions 진행 상황 모니터링
- [ ] Manual Approval 확인 및 승인
- [ ] Health Check 통과 확인

### 배포 후

- [ ] 프로덕션 URL 접속 확인
- [ ] Grafana 대시보드에서 메트릭 확인
- [ ] 주요 기능 동작 확인
- [ ] 에러 로그 확인

---

## 🎯 빠른 참조

### 일반적인 배포

```bash
# 1. 코드 커밋
git add .
git commit -m "feat: 새 기능"
git push origin master

# 2. 태그 생성 & 푸시
git tag -a v1.1.0 -m "Release v1.1.0: 새 기능 추가" && git push origin v1.1.0

# 3. GitHub Actions에서 승인 → 완료!
```

### 핫픽스 배포

```bash
# 1. 버그 수정 커밋
git add .
git commit -m "fix: 긴급 버그 수정"
git push origin master

# 2. Patch 버전 태그
git tag -a v1.0.1 -m "Hotfix v1.0.1: 긴급 버그 수정" && git push origin v1.0.1

# 3. 승인 후 즉시 배포
```

### 긴급 롤백

```bash
# GitHub Actions → Rollback Deployment
# version: v1.0.0
# reason: Critical production issue
# → 승인 → 롤백 완료
```

---

## 🌐 프로덕션 환경

- **Frontend**: https://gijun.net
- **API**: https://api.gijun.net
- **Server**: 172.30.1.79 (gijunpark)
- **Grafana**: http://172.30.1.79:3001
- **Prometheus**: http://172.30.1.79:9091
- **Eureka**: http://172.30.1.79:8761

---

## 📞 문의

문제가 발생하거나 도움이 필요하면:
1. GitHub Actions 로그 확인
2. 서버 로그 확인
3. Grafana 메트릭 확인
4. GitHub Issues에 문의

---

**마지막 업데이트**: 2026-01-29  
**버전**: v1.0.0
