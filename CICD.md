## 🚀 GitHub Actions CI/CD

이 프로젝트는 GitHub Actions를 사용한 자동화된 CI/CD 파이프라인을 구축하였습니다.

### 📋 워크플로우

1. **CI (Continuous Integration)** - 모든 push/PR에서 자동 실행
   - Backend 테스트 & 빌드
   - Frontend 린트 & 빌드

2. **Production 배포** - Git tag (v*.*.*) 생성시 실행
   - Docker 이미지 빌드 & 푸시
   - 수동 승인 필요
   - Rolling update 배포
   - 자동 헬스체크 & 롤백

3. **Rollback** - 수동 실행
   - 이전 버전으로 롤백

4. **Security Scan** - 주 1회 자동 실행
   - Trivy, CodeQL, OWASP Dependency Check

### 🔧 빠른 설정

```bash
# 1. SSH Key 생성 (서버에서)
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions
cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys

# 2. GitHub Secret 설정
# Repository → Settings → Secrets → New secret
# Name: SSH_PRIVATE_KEY
# Value: ~/.ssh/github_actions 내용

# 3. GitHub Environments 생성
# Repository → Settings → Environments
# - production (Required reviewers 설정)
# - production-approval (Required reviewers 설정)
# - production-rollback (Required reviewers 설정)

# 4. 첫 배포
git tag -a v1.0.0 -m "First production release"
git push origin v1.0.0
```

### 📚 상세 문서

- [빠른 시작 가이드](.github/QUICKSTART.md)
- [상세 설정 가이드](.github/SETUP_GUIDE.md)
- [워크플로우 가이드](.github/README.md)

### 🌐 프로덕션 환경

- **Frontend**: https://gijun.net
- **API**: https://api.gijun.net
- **Server**: 172.30.1.79 (gijunpark)
- **Monitoring**: http://172.30.1.79:3001 (Grafana)

### 🔄 배포 방법

```bash
# 버전 태그 생성
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# GitHub Actions에서 수동 승인 후 자동 배포
```

### 🆘 롤백 방법

1. GitHub → Actions → **Rollback Deployment**
2. Run workflow
3. Version 입력 (예: v1.0.0)
4. Reason 입력
5. 수동 승인 후 롤백 진행

---

자세한 내용은 [.github/README.md](.github/README.md)를 참조하세요.
