#!/bin/bash
# =============================================================================
# Stock-Simulator Kubernetes Cluster Setup Script
# 사전 요구사항: kubectl, helm, k3s/k8s 클러스터
# =============================================================================

set -e

echo "=========================================="
echo "Stock-Simulator K8s Cluster Setup"
echo "=========================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수: 로그 출력
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 함수: kubectl 확인
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl이 설치되어 있지 않습니다."
        exit 1
    fi
    log_info "kubectl 확인 완료"
}

# 함수: 클러스터 연결 확인
check_cluster() {
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Kubernetes 클러스터에 연결할 수 없습니다."
        exit 1
    fi
    log_info "클러스터 연결 확인 완료"
}

# 함수: Nginx Ingress Controller 설치
install_nginx_ingress() {
    log_info "Nginx Ingress Controller 설치 중..."
    
    if kubectl get namespace ingress-nginx &> /dev/null; then
        log_warn "Nginx Ingress가 이미 설치되어 있습니다."
        return
    fi
    
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.5/deploy/static/provider/baremetal/deploy.yaml
    
    log_info "Nginx Ingress Controller 설치 완료"
}

# 함수: Local Path Provisioner 설치 (K3s에 기본 포함, 다른 클러스터용)
install_local_path_provisioner() {
    log_info "Local Path Provisioner 확인 중..."
    
    if kubectl get storageclass local-path &> /dev/null; then
        log_warn "Local Path Provisioner가 이미 설치되어 있습니다."
        return
    fi
    
    kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.26/deploy/local-path-storage.yaml
    
    # Default StorageClass로 설정
    kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
    
    log_info "Local Path Provisioner 설치 완료"
}

# 함수: Namespace 생성
create_namespaces() {
    log_info "Namespace 생성 중..."
    
    kubectl apply -f infra/k8s/base/namespaces.yaml
    
    log_info "Namespace 생성 완료"
}

# 함수: Secrets 생성
create_secrets() {
    log_info "Secrets 생성 중..."
    
    kubectl apply -f infra/k8s/base/secrets.yaml
    
    log_info "Secrets 생성 완료"
}

# 함수: MySQL ConfigMap 생성
create_mysql_config() {
    log_info "MySQL ConfigMap 생성 중..."
    
    kubectl apply -f infra/k8s/databases/mysql/configmap.yaml
    
    log_info "MySQL ConfigMap 생성 완료"
}

# 메인 실행
main() {
    log_info "Setup 시작..."
    
    check_kubectl
    check_cluster
    
    # 필수 컴포넌트 설치
    install_nginx_ingress
    install_local_path_provisioner
    
    # 기본 리소스 생성
    create_namespaces
    create_secrets
    create_mysql_config
    
    log_info "=========================================="
    log_info "기본 설정 완료!"
    log_info ""
    log_info "다음 단계:"
    log_info "1. ArgoCD 설치: ./infra/scripts/setup-argocd.sh"
    log_info "2. 또는 수동으로 리소스 배포:"
    log_info "   kubectl apply -f infra/k8s/databases/ -R"
    log_info "   kubectl apply -f infra/k8s/infrastructure/ -R"
    log_info "   kubectl apply -f infra/k8s/apps/ -R"
    log_info "   kubectl apply -f infra/k8s/monitoring/ -R"
    log_info "=========================================="
}

main "$@"
