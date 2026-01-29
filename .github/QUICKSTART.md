# GitHub Actions 빠른 시작 가이드

## 🚀 5분 안에 시작하기

### 1️⃣ SSH Key 설정

```bash
# 서버에서 실행
ssh gijunpark@172.30.1.79
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions
cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys
cat ~/.ssh/github_actions  # 이 내용을 복사
```

### 2️⃣ GitHub Secrets 설정

1. GitHub Repository → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret** 클릭
3. Name: `SSH_PRIVATE_KEY`
4. Value: 위에서 복사한 SSH private key 붙여넣기
5. **Add secret** 클릭

### 3️⃣ Environments 생성

1. GitHub Repository → **Settings** → **Environments**
2. 다음 3개 환경 생성:
   - `production` (Required reviewers 설정)
   - `production-approval` (Required reviewers 설정)
   - `production-rollback` (Required reviewers 설정)

### 4️⃣ 첫 배포

```bash
# 로컬에서 태그 생성
git tag -a v1.0.0 -m "First production release"
git push origin v1.0.0

# GitHub Actions에서 승인 후 배포 완료!
```

---

## 📖 상세 가이드

자세한 설정 방법은 [SETUP_GUIDE.md](SETUP_GUIDE.md)를 참조하세요.

---

## 🔄 배포 플로우

```
main 브랜치 → CI 자동 실행 (테스트)
                    ↓
           태그 생성 (v1.0.0)
                    ↓
         Docker 이미지 빌드 & 푸시
                    ↓
          🚨 Manual Approval 승인
                    ↓
          프로덕션 백업 & 배포
                    ↓
              헬스체크
                    ↓
            ✅ 배포 완료!
```

---

## 🆘 문제 발생시

### 배포 실패
→ GitHub Actions → Rollback Deployment 실행

### 서버 확인
```bash
ssh gijunpark@172.30.1.79
docker-compose --profile all ps
docker logs <container-name>
```

---

## 📊 모니터링

- **Grafana**: http://172.30.1.79:3001 (admin / stocksim123)
- **Eureka**: http://172.30.1.79:8761
- **Frontend**: https://gijun.net
- **API**: https://api.gijun.net

---

## ✅ 필수 확인사항

- [ ] SSH_PRIVATE_KEY secret 설정
- [ ] 3개 Environment 생성 (reviewers 설정)
- [ ] 서버 Docker 실행 중
- [ ] .env 파일 설정 완료
