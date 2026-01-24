#!/bin/bash
# =============================================================================
# ArgoCD Setup Script
# =============================================================================

set -e

echo "=========================================="
echo "ArgoCD Installation & Configuration"
echo "=========================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ArgoCD 설치
install_argocd() {
    log_info "ArgoCD 설치 중..."
    
    # Namespace 생성 (이미 있으면 무시)
    kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
    
    # ArgoCD 설치 (Stable 버전)
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
    
    log_info "ArgoCD 설치 완료. Pod 시작 대기 중..."
    
    # Pod가 준비될 때까지 대기
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s
    
    log_info "ArgoCD Server가 준비되었습니다."
}

# ArgoCD CLI 설치 안내
install_argocd_cli() {
    log_info "ArgoCD CLI 설치 안내:"
    echo ""
    echo "Linux:"
    echo "  curl -sSL -o argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64"
    echo "  chmod +x argocd && sudo mv argocd /usr/local/bin/"
    echo ""
    echo "Mac:"
    echo "  brew install argocd"
    echo ""
    echo "Windows:"
    echo "  choco install argocd-cli"
    echo ""
}

# 초기 Admin 비밀번호 가져오기
get_admin_password() {
    log_info "ArgoCD 초기 Admin 비밀번호 가져오는 중..."
    
    ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
    
    echo ""
    echo "=========================================="
    echo "ArgoCD Admin 로그인 정보"
    echo "=========================================="
    echo "Username: admin"
    echo "Password: $ARGOCD_PASSWORD"
    echo "=========================================="
    echo ""
}

# ArgoCD Server 외부 접근 설정
expose_argocd() {
    log_info "ArgoCD Server 외부 접근 설정 중..."
    
    # NodePort로 변경 (또는 LoadBalancer)
    kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort", "ports": [{"port": 443, "nodePort": 30443, "name": "https"}]}}'
    
    log_info "ArgoCD Server가 NodePort 30443으로 노출되었습니다."
    echo ""
    echo "접속 URL: https://172.30.1.79:30443"
    echo "또는 Ingress 사용: https://argocd.172.30.1.79.nip.io"
    echo ""
}

# ArgoCD Project 및 Application 설정
setup_argocd_apps() {
    log_info "ArgoCD Project 및 Application 설정 중..."
    
    # Project 생성
    kubectl apply -f infra/k8s/argocd/projects/stocksim-project.yaml
    
    log_info "ArgoCD Project 생성 완료"
    
    # Root Application 생성 (App of Apps)
    # 주의: Git 저장소 URL을 실제 저장소로 변경 필요
    log_warn "Git 저장소 URL을 실제 저장소로 변경한 후 Applications를 배포하세요."
    log_info "파일 위치: infra/k8s/argocd/applications/*.yaml"
    
    echo ""
    echo "Applications 배포 방법:"
    echo "1. infra/k8s/argocd/applications/*.yaml 파일의 repoURL을 실제 저장소로 변경"
    echo "2. kubectl apply -f infra/k8s/argocd/applications/root-app.yaml"
    echo ""
}

# 메인 실행
main() {
    log_info "ArgoCD Setup 시작..."
    
    install_argocd
    install_argocd_cli
    get_admin_password
    expose_argocd
    setup_argocd_apps
    
    log_info "=========================================="
    log_info "ArgoCD Setup 완료!"
    log_info ""
    log_info "다음 단계:"
    log_info "1. ArgoCD UI 접속: https://172.30.1.79:30443"
    log_info "2. 위 로그인 정보로 로그인"
    log_info "3. Git 저장소 URL 수정 후 Applications 배포"
    log_info "=========================================="
}

main "$@"
