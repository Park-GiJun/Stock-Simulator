#!/bin/bash

# Rollback to previous version
# Usage: ./rollback.sh [tag_version]

set -e

TAG_VERSION=${1:-v1.3.1}
PROJECT_DIR="/home/jun/Stock-Simulator"

echo "Rolling back to version: ${TAG_VERSION}"

cd ${PROJECT_DIR}

git fetch --tags --force
git checkout ${TAG_VERSION}

./gradlew clean build -x test --no-daemon

docker-compose --profile all down
docker-compose --profile all build
docker-compose --profile all up -d

echo "Rollback completed! Version: ${TAG_VERSION}"
