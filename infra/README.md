# =============================================================================
# Stock-Simulator Infrastructure Documentation
# =============================================================================

# 🏗️ 인프라 아키텍처

## 📋 목차
1. [개요](#개요)
2. [DB 구조](#db-구조)
3. [Kubernetes 구조](#kubernetes-구조)
4. [ArgoCD GitOps](#argocd-gitops)
5. [배포 가이드](#배포-가이드)
6. [운영 가이드](#운영-가이드)



## 개요

Stock-Simulator는 마이크로서비스 아키텍처 기반의 모의 주식 거래 게임입니다.

### 기술 스택
- **Orchestration**: Kubernetes (K3s 또는 K8s)
- **CI/CD**: ArgoCD (GitOps)
- **Database**: MySQL (Master-Replica), MongoDB (ReplicaSet), Redis (Sentinel)
- **Message Broker**: Apache Kafka (KRaft 모드)
- **Search**: Elasticsearch
- **Monitoring**: Prometheus + Grafana

### 서버 정보
- **Server IP**: 172.30.1.79
- **Frontend**: http://172.30.1.79/
- **API Gateway**: http://172.30.1.79/api/
- **ArgoCD**: https://172.30.1.79:30443
- **Grafana**: http://172.30.1.79/grafana

---

## DB 구조

### MySQL - 서비스별 분리 (Master + Replica)

| 서비스 | Master Host | Replica Host | Database |
|--------|-------------|--------------|----------|
| user-service | mysql-user-master | mysql-user-read | userdb |
| stock-service | mysql-stock-master | mysql-stock-read | stockdb |
| trading-service | mysql-trading-master | mysql-trading-read | tradingdb |
| event-service | mysql-event-master | mysql-event-read | eventdb |
| scheduler-service | mysql-scheduler-master | mysql-scheduler-read | schedulerdb |
| season-service | mysql-season-master | mysql-season-read | seasondb |

**Read/Write 분리**:
- Write 작업 → Master
- Read 작업 → Replica (로드밸런싱)

### MongoDB ReplicaSet

```
mongodb-0 (Primary) ─┬─► mongodb-1 (Secondary)
                     └─► mongodb-2 (Secondary)
```

- 사용 서비스: news-service, event-service (로그)
- ReadPreference: secondaryPreferred

### Redis Sentinel

```
redis-master ◄─── redis-sentinel-0
     │            redis-sentinel-1
     │            redis-sentinel-2
     ▼
redis-replica-0
redis-replica-1
```

- 자동 Failover 지원
- 사용: 세션, 캐시, 실시간 데이터

---

## Kubernetes 구조

### Namespace 구조

```
├── stocksim-apps        # 애플리케이션 서비스
├── stocksim-db          # 데이터베이스
├── stocksim-infra       # Kafka, Elasticsearch
├── stocksim-monitoring  # Prometheus, Grafana
└── argocd               # ArgoCD
```

### 서비스 구성

```
                    ┌─────────────────┐
                    │   Ingress       │
                    │ (172.30.1.79)   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
        ┌─────────┐   ┌───────────┐   ┌─────────┐
        │Frontend │   │API Gateway│   │ Grafana │
        └─────────┘   └─────┬─────┘   └─────────┘
                            │
           ┌────────────────┼────────────────┐
           │                │                │
           ▼                ▼                ▼
      ┌─────────┐     ┌──────────┐    ┌──────────┐
      │ Eureka  │◄────│ Services │────│  Kafka   │
      └─────────┘     └────┬─────┘    └──────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
         ┌───────┐   ┌─────────┐  ┌─────────┐
         │ MySQL │   │ MongoDB │  │  Redis  │
         │ (M/R) │   │  (RS)   │  │(Sentinel│
         └───────┘   └─────────┘  └─────────┘
```

---

## ArgoCD GitOps

### Application 구조 (App of Apps)

```
stocksim-root (Root App)
    │
    ├── stocksim-databases    → infra/k8s/databases/
    ├── stocksim-infrastructure → infra/k8s/infrastructure/
    ├── stocksim-apps         → infra/k8s/apps/
    └── stocksim-monitoring   → infra/k8s/monitoring/
```

### 배포 흐름

```
1. 개발자 → Git Push
         ↓
2. GitHub Actions → Docker Build → Registry Push
         ↓
3. GitHub Actions → K8s Manifest 업데이트 (이미지 태그)
         ↓
4. ArgoCD → Git 변경 감지 → 자동 Sync → K8s 배포
```

---

## 배포 가이드

### 1. 사전 요구사항

```bash
# kubectl 설치
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl && sudo mv kubectl /usr/local/bin/

# K3s 설치 (경량 K8s)
curl -sfL https://get.k3s.io | sh -

# kubeconfig 설정
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $(id -u):$(id -g) ~/.kube/config
```

### 2. 클러스터 초기 설정

```bash
# 스크립트 실행 권한 부여
chmod +x infra/scripts/*.sh

# 클러스터 기본 설정
./infra/scripts/setup-cluster.sh

# ArgoCD 설치
./infra/scripts/setup-argocd.sh
```

### 3. Git 저장소 설정

ArgoCD Application 파일에서 Git URL 수정:
```yaml
# infra/k8s/argocd/applications/*.yaml
spec:
  source:
    repoURL: https://github.com/YOUR_USERNAME/Stock-Simulator.git
```

### 4. ArgoCD Application 배포

```bash
# Project 생성
kubectl apply -f infra/k8s/argocd/projects/stocksim-project.yaml

# Root Application 배포 (모든 하위 앱 자동 생성)
kubectl apply -f infra/k8s/argocd/applications/root-app.yaml
```

### 5. 수동 배포 (ArgoCD 없이)

```bash
# Namespace & Secrets
kubectl apply -f infra/k8s/base/

# Databases
kubectl apply -f infra/k8s/databases/ -R

# Infrastructure
kubectl apply -f infra/k8s/infrastructure/ -R

# Applications
kubectl apply -f infra/k8s/apps/ -R

# Monitoring
kubectl apply -f infra/k8s/monitoring/ -R
```

---

## 운영 가이드

### Pod 상태 확인

```bash
# 모든 서비스 상태
kubectl get pods -n stocksim-apps

# DB 상태
kubectl get pods -n stocksim-db

# 로그 확인
kubectl logs -f deployment/user-service -n stocksim-apps
```

### MySQL Replication 상태 확인

```bash
# Master 확인
kubectl exec -it mysql-user-master-0 -n stocksim-db -- \
  mysql -u root -proot123 -e "SHOW MASTER STATUS\G"

# Replica 상태 확인
kubectl exec -it mysql-user-replica-0 -n stocksim-db -- \
  mysql -u root -proot123 -e "SHOW SLAVE STATUS\G"
```

### Redis Sentinel 상태

```bash
kubectl exec -it redis-sentinel-0 -n stocksim-db -- \
  redis-cli -p 26379 SENTINEL masters
```

### ArgoCD UI 접속

```bash
# 초기 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d

# 접속: https://172.30.1.79:30443
# Username: admin
```

### 스케일링

```bash
# Deployment 스케일
kubectl scale deployment user-service --replicas=3 -n stocksim-apps

# HPA 적용 (자동 스케일링)
kubectl autoscale deployment user-service \
  --cpu-percent=70 --min=2 --max=5 -n stocksim-apps
```

### 롤백

```bash
# ArgoCD UI에서 이전 버전으로 Sync
# 또는 CLI:
argocd app rollback stocksim-apps
```

---

## 트러블슈팅

### DB 연결 실패
```bash
# DNS 확인
kubectl exec -it [pod] -- nslookup mysql-user-master.stocksim-db

# 네트워크 정책 확인
kubectl get networkpolicy -n stocksim-db
```

### Pod CrashLoopBackOff
```bash
# 이벤트 확인
kubectl describe pod [pod-name] -n stocksim-apps

# 이전 로그 확인
kubectl logs [pod-name] --previous -n stocksim-apps
```

### ArgoCD Sync 실패
```bash
# Application 상태 확인
kubectl get application -n argocd

# 상세 정보
argocd app get stocksim-apps
```

---

## 연락처

문제 발생 시 이슈 생성: https://github.com/YOUR_USERNAME/Stock-Simulator/issues
