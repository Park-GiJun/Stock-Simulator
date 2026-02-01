#!/bin/bash

# Stock Simulator Deployment Script with Git Tag
# This script should be run on the server

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
TAG_VERSION=${1:-v1.3.2}
PROJECT_DIR="/home/jun/Stock-Simulator"
GRADLE_BUILD_OPTIONS="-x test --no-daemon"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Stock Simulator Deployment - Tag: ${TAG_VERSION}${NC}"
echo -e "${GREEN}========================================${NC}"

# Step 1: Navigate to project directory
echo -e "\n${YELLOW}[1/8] Navigating to project directory...${NC}"
cd ${PROJECT_DIR}
pwd

# Step 2: Fetch latest tags and checkout
echo -e "\n${YELLOW}[2/8] Fetching latest tags from remote...${NC}"
git fetch --tags --force

echo -e "\n${YELLOW}[3/8] Checking out tag ${TAG_VERSION}...${NC}"
git checkout ${TAG_VERSION}

# Step 3: Clean previous builds
echo -e "\n${YELLOW}[4/8] Cleaning previous builds...${NC}"
./gradlew clean ${GRADLE_BUILD_OPTIONS}

# Step 4: Build all services
echo -e "\n${YELLOW}[5/8] Building all services...${NC}"
./gradlew build ${GRADLE_BUILD_OPTIONS}

# Step 5: Stop existing containers
echo -e "\n${YELLOW}[6/8] Stopping existing containers...${NC}"
docker-compose --profile all down || true

# Step 6: Rebuild Docker images
echo -e "\n${YELLOW}[7/8] Building Docker images...${NC}"
docker-compose --profile all build --no-cache

# Step 7: Start all services
echo -e "\n${YELLOW}[8/8] Starting all services...${NC}"
docker-compose --profile all up -d

# Wait for services to start
echo -e "\n${GREEN}Waiting for services to start...${NC}"
sleep 10

# Check container status
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Container Status:${NC}"
echo -e "${GREEN}========================================${NC}"
docker-compose --profile all ps

# Check Eureka
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Checking Eureka Server...${NC}"
echo -e "${GREEN}========================================${NC}"
sleep 5
curl -s http://localhost:8761/actuator/health | jq '.' || echo "Eureka not ready yet"

# Show logs for critical services
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Recent logs from services:${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}Eureka Server:${NC}"
docker logs stockSimulator-eureka-server --tail 20

echo -e "\n${YELLOW}API Gateway:${NC}"
docker logs stockSimulator-api-gateway --tail 20

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${GREEN}Deployed version: ${TAG_VERSION}${NC}"
echo -e "${GREEN}Eureka Dashboard: http://localhost:8761${NC}"
echo -e "${GREEN}API Gateway: http://localhost:9832${NC}"
echo -e "${GREEN}Grafana: http://localhost:3001 (admin/stocksim123)${NC}"
echo -e "${GREEN}Prometheus: http://localhost:9091${NC}"
echo -e "${GREEN}Kafka UI: http://localhost:8089${NC}"
echo -e "\n${YELLOW}To view logs: docker logs stockSimulator-<service-name> -f${NC}"
echo -e "${YELLOW}To check status: docker-compose --profile all ps${NC}"
