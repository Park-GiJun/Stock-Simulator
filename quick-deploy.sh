#!/bin/bash

# Stock Simulator Quick Deployment Script
# For rapid deployment with minimal checks

set -e

TAG_VERSION=${1:-v1.3.2}
PROJECT_DIR="/home/jun/Stock-Simulator"

echo "Quick deploying Stock Simulator - Tag: ${TAG_VERSION}"

cd ${PROJECT_DIR}

# Fetch and checkout
git fetch --tags --force
git checkout ${TAG_VERSION}

# Build
echo "Building services..."
./gradlew clean build -x test --no-daemon

# Restart containers
echo "Restarting containers..."
docker-compose --profile all down
docker-compose --profile all build
docker-compose --profile all up -d

echo "Deployment completed! Waiting for services..."
sleep 15

docker-compose --profile all ps
echo "Done! Version: ${TAG_VERSION}"
