# Nginx 설정 가이드

## 📋 파일 설명

| 파일 | 설명 |
|------|------|
| `gijun.net` | K8s Ingress를 통한 연결 (권장) |
| `gijun.net.nodeport` | NodePort 직접 연결 (Ingress 없이) |

## 🚀 설치 방법

### 1. Nginx 설정 파일 복사

```bash
# 권장: NodePort 직접 연결 방식
sudo cp infra/nginx/gijun.net.nodeport /etc/nginx/sites-available/gijun.net

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
sudo certbot --nginx -d gijun.net -d www.gijun.net

# 자동 갱신 테스트
sudo certbot renew --dry-run
```

## 🔌 NodePort 매핑

| 서비스 | NodePort | 설명 |
|--------|----------|------|
| Frontend | 30000 | SvelteKit 앱 |
| API Gateway | 30080 | 백엔드 API |
| Eureka | 30761 | 서비스 디스커버리 |
| ArgoCD | 30443 | GitOps (HTTPS) |
| Grafana | 30300 | 모니터링 대시보드 |
| Prometheus | 30090 | 메트릭 수집 |
| Kafka UI | 30089 | 메시지 큐 모니터링 |
| Kibana | 30561 | 로그 검색 |

## 🌐 접속 URL

설정 완료 후 다음 URL로 접속 가능합니다:

| 서비스 | URL |
|--------|-----|
| 메인 (Frontend) | https://gijun.net/ |
| API | https://gijun.net/api/ |
| Swagger UI | https://gijun.net/swagger-ui |
| Eureka | https://gijun.net/eureka |
| ArgoCD | https://gijun.net/argocd |
| Grafana | https://gijun.net/grafana/ |
| Prometheus | https://gijun.net/prometheus/ |
| Kafka UI | https://gijun.net/kafka-ui/ |
| Kibana | https://gijun.net/kibana/ |

## 🔧 K8s NodePort 서비스 적용

Nginx가 K8s 서비스에 연결하려면 NodePort 서비스가 필요합니다:

```bash
kubectl apply -f infra/k8s/infrastructure/nodeport-services.yaml
```

## ⚠️ 문제 해결

### 502 Bad Gateway
- K8s Pod가 정상 실행 중인지 확인
- NodePort 서비스가 올바르게 설정되었는지 확인

```bash
# Pod 상태 확인
kubectl get pods -n stocksim-apps

# 서비스 확인
kubectl get svc -n stocksim-apps
```

### Connection Refused
- 방화벽에서 NodePort 포트가 열려있는지 확인

```bash
# UFW 사용시
sudo ufw allow 30000:32767/tcp
```

### SSL 인증서 오류
```bash
# 인증서 갱신
sudo certbot renew

# 인증서 상태 확인
sudo certbot certificates
```
