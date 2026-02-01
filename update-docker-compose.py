#!/usr/bin/env python3
import re

with open('docker-compose.yml', 'r', encoding='utf-8') as f:
    content = f.read()

services = ['eureka-server', 'api-gateway', 'user-service', 'stock-service', 
            'trading-service', 'event-service', 'scheduler-service', 'news-service']

for service in services:
    # build 섹션 제거 패턴
    build_pattern = rf'''(  {service}:
    image: stocksim/{service}:latest
    container_name: stockSimulator-{service}
    profiles: \["services", "all"\])
    build:
      context: ./backend/{service}
      dockerfile: Dockerfile'''
    
    replacement = rf'''\1'''
    content = re.sub(build_pattern, replacement, content, flags=re.MULTILINE)
    
    # 이미지 경로 변경
    old_image = f'image: stocksim/{service}:latest'
    new_image = f'image: ${{DOCKER_REGISTRY:-ghcr.io/park-gijun/stocksim}}/{service}:${{IMAGE_TAG:-latest}}'
    content = content.replace(old_image, new_image)

# frontend 이미지도 변경
content = content.replace(
    'image: stocksim/frontend:latest',
    'image: ${DOCKER_REGISTRY:-ghcr.io/park-gijun/stocksim}/frontend:${IMAGE_TAG:-latest}'
)

# frontend build 섹션 제거
frontend_build_pattern = r'''(  frontend:
    image: \$\{DOCKER_REGISTRY:-ghcr\.io/park-gijun/stocksim\}/frontend:\$\{IMAGE_TAG:-latest\}
    container_name: stockSimulator-frontend
    profiles: \["frontend", "all"\])
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        - VITE_API_URL=https://api.gijun.net'''
content = re.sub(frontend_build_pattern, r'\1', content, flags=re.MULTILINE)

with open('docker-compose.yml', 'w', encoding='utf-8') as f:
    f.write(content)

print('Success: docker-compose.yml updated to use GHCR images')
