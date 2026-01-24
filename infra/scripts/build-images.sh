#!/bin/bash
# =============================================================================
# Docker Image Build & Push Script
# 모든 서비스의 Docker 이미지를 빌드하고 Registry에 Push
# =============================================================================

set -e

# 설정
REGISTRY="${REGISTRY:-docker.io}"
REPOSITORY="${REPOSITORY:-stocksim}"
TAG="${TAG:-latest}"

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

# Backend 서비스 목록
BACKEND_SERVICES=(
    "eureka-server"
    "api-gateway"
    "user-service"
    "stock-service"
    "trading-service"
    "event-service"
    "news-service"
    "scheduler-service"
    "season-service"
)

# Backend 서비스 빌드
build_backend_service() {
    local service=$1
    local image_name="${REGISTRY}/${REPOSITORY}/${service}:${TAG}"
    
    log_info "Building ${service}..."
    
    docker build \
        --build-arg SERVICE_NAME=${service} \
        -t ${image_name} \
        -f infra/docker/backend/Dockerfile \
        backend/
    
    log_info "Built: ${image_name}"
}

# Frontend 빌드
build_frontend() {
    local image_name="${REGISTRY}/${REPOSITORY}/frontend:${TAG}"
    
    log_info "Building frontend..."
    
    docker build \
        --build-arg PUBLIC_API_URL=http://172.30.1.79/api \
        --build-arg PUBLIC_WS_URL=ws://172.30.1.79/ws \
        -t ${image_name} \
        -f infra/docker/frontend/Dockerfile \
        .
    
    log_info "Built: ${image_name}"
}

# 이미지 Push
push_image() {
    local image_name=$1
    
    log_info "Pushing ${image_name}..."
    docker push ${image_name}
    log_info "Pushed: ${image_name}"
}

# 모든 이미지 Push
push_all() {
    for service in "${BACKEND_SERVICES[@]}"; do
        push_image "${REGISTRY}/${REPOSITORY}/${service}:${TAG}"
    done
    
    push_image "${REGISTRY}/${REPOSITORY}/frontend:${TAG}"
}

# 사용법 출력
usage() {
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  build-all      Build all services"
    echo "  build-backend  Build all backend services"
    echo "  build-frontend Build frontend only"
    echo "  build <name>   Build specific service"
    echo "  push-all       Push all images to registry"
    echo "  push <name>    Push specific image"
    echo ""
    echo "Options:"
    echo "  --registry     Docker registry (default: docker.io)"
    echo "  --repository   Repository name (default: stocksim)"
    echo "  --tag          Image tag (default: latest)"
    echo ""
    echo "Examples:"
    echo "  $0 build-all --tag v1.0.0"
    echo "  $0 build user-service"
    echo "  $0 push-all --registry ghcr.io"
}

# 메인 실행
main() {
    local command=$1
    shift || true
    
    # 옵션 파싱
    while [[ $# -gt 0 ]]; do
        case $1 in
            --registry)
                REGISTRY="$2"
                shift 2
                ;;
            --repository)
                REPOSITORY="$2"
                shift 2
                ;;
            --tag)
                TAG="$2"
                shift 2
                ;;
            *)
                break
                ;;
        esac
    done
    
    case $command in
        build-all)
            for service in "${BACKEND_SERVICES[@]}"; do
                build_backend_service $service
            done
            build_frontend
            ;;
        build-backend)
            for service in "${BACKEND_SERVICES[@]}"; do
                build_backend_service $service
            done
            ;;
        build-frontend)
            build_frontend
            ;;
        build)
            local service=$1
            if [ -z "$service" ]; then
                log_error "Service name required"
                exit 1
            fi
            if [ "$service" == "frontend" ]; then
                build_frontend
            else
                build_backend_service $service
            fi
            ;;
        push-all)
            push_all
            ;;
        push)
            local service=$1
            if [ -z "$service" ]; then
                log_error "Service name required"
                exit 1
            fi
            push_image "${REGISTRY}/${REPOSITORY}/${service}:${TAG}"
            ;;
        *)
            usage
            exit 1
            ;;
    esac
}

main "$@"
