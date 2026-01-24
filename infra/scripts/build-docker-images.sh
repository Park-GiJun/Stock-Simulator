#!/bin/bash
# =============================================================================
# Stock Simulator - Docker Image Build Script
# =============================================================================
# 사용법:
#   ./build-docker-images.sh          # 모든 서비스 빌드
#   ./build-docker-images.sh eureka   # 특정 서비스만 빌드
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${SCRIPT_DIR}/../backend"
REGISTRY="${REGISTRY:-stocksim}"

# 서비스 목록
SERVICES=(
    "eureka-server"
    "api-gateway"
    "user-service"
    "stock-service"
    "trading-service"
    "event-service"
    "scheduler-service"
    "news-service"
    "season-service"
)

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Gradle 빌드
build_gradle() {
    log_info "Building all services with Gradle..."
    cd "${BACKEND_DIR}"
    ./gradlew clean build -x test
    log_info "Gradle build completed!"
}

# Docker 이미지 빌드
build_docker_image() {
    local service=$1
    local service_dir="${BACKEND_DIR}/${service}"
    
    if [ ! -d "${service_dir}" ]; then
        log_error "Service directory not found: ${service_dir}"
        return 1
    fi
    
    if [ ! -f "${service_dir}/Dockerfile" ]; then
        log_error "Dockerfile not found: ${service_dir}/Dockerfile"
        return 1
    fi
    
    # JAR 파일 확인
    local jar_file=$(find "${service_dir}/build/libs" -name "*.jar" -not -name "*-plain.jar" 2>/dev/null | head -1)
    if [ -z "${jar_file}" ]; then
        log_error "JAR file not found for ${service}. Please run Gradle build first."
        return 1
    fi
    
    log_info "Building Docker image for ${service}..."
    docker build -t "${REGISTRY}/${service}:latest" "${service_dir}"
    log_info "Docker image built: ${REGISTRY}/${service}:latest"
}

# 메인 실행
main() {
    local target_service=$1
    
    # Gradle 빌드
    build_gradle
    
    echo ""
    log_info "Starting Docker image builds..."
    echo ""
    
    if [ -n "${target_service}" ]; then
        # 특정 서비스만 빌드
        build_docker_image "${target_service}"
    else
        # 모든 서비스 빌드
        for service in "${SERVICES[@]}"; do
            build_docker_image "${service}"
            echo ""
        done
    fi
    
    echo ""
    log_info "All Docker images built successfully!"
    echo ""
    log_info "Built images:"
    docker images | grep "${REGISTRY}"
}

main "$@"
