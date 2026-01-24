# Nginx 설정 가이드

## 📋 파일 설명

| 파일 | 설명 |
|------|------|
| `gijun.net` | Docker 컨테이너 연결 (운영 환경) |
| `nginx.conf` | Docker 내부 프록시용 (개발 환경) |

## 🚀 설치 방법

### 1. Nginx 설정 파일 복사

```bash
# 설정 파일 복사
sudo cp infra/nginx/gijun.net /etc/nginx/sites-available/gijun.net

# 심볼릭 링크 생성
sudo ln -sf /etc/nginx/sites-available/gijun.net /etc/nginx/sites-enabled/

# 기존 default 비활성화
sudo rm -f /etc/nginx/sites-enabled/default
```

### 2. Nginx 설정 테스트

```bash
sudo nginx -t
```

### 3. Nginx 재시작

```bash
sudo systemctl reload nginx
# 또는
sudo systemctl restart nginx
```

### 4. SSL 인증서 (Let's Encrypt)

이미 SSL이 설정되어 있다면 생략 가능합니다.

```bash
# Certbot 설치
sudo apt install certbot python3-certbot-nginx -y

# SSL 인증서 발급
sudo certbot --nginx -d gijun.net -d api.gijun.net

# 자동 갱신 테스트
sudo certbot renew --dry-run
```

## 🔌 Docker 포트 매핑

| 서비스 | 포트 | 설명 |
|--------|------|------|
| Frontend | 8080 | SvelteKit 앱 |
| API Gateway | 9832 | 백엔드 API |
| Eureka | 8761 | 서비스 디스커버리 |
| Grafana | 3001 | 모니터링 대시보드 |
| Prometheus | 9091 | 메트릭 수집 |
| Kafka UI | 8089 | 메시지 큐 모니터링 |

## 🌐 접속 URL

설정 완료 후 다음 URL로 접속 가능합니다:

| 서비스 | URL |
|--------|-----|
| 메인 (Frontend) | https://gijun.net/ |
| API | https://api.gijun.net/ |
| Eureka | http://localhost:8761/ |
| Grafana | http://localhost:3001/ |
| Prometheus | http://localhost:9091/ |
| Kafka UI | http://localhost:8089/ |

## ⚠️ 문제 해결

### 502 Bad Gateway
- Docker 컨테이너가 정상 실행 중인지 확인

```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f api-gateway
```

### Connection Refused
- Docker 컨테이너가 실행 중인지 확인
- 포트가 올바르게 매핑되었는지 확인

```bash
# 포트 확인
docker port stockSimulator-api-gateway
```

### SSL 인증서 오류
```bash
# 인증서 갱신
sudo certbot renew

# 인증서 상태 확인
sudo certbot certificates
```
