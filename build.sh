#!/bin/bash

# Docker Hub 사용자 이름을 입력하세요.
DOCKERHUB_USERNAME="beatchoi156"

# 서비스 목록
SERVICES=(
  "account-service"
  "discovery-service"
  "domain-service"
  "gateway-service"
  "settlement-service"
)

# buildx builder 설정 (멀티 플랫폼 지원)
# echo "Setting up Docker buildx for multi-platform builds..."
# docker buildx create --name multiarch --use 2>/dev/null || docker buildx use multiarch

# 각 서비스에 대해 Docker 이미지 빌드 및 푸시
for SERVICE in "${SERVICES[@]}"; do
  IMAGE_NAME="$DOCKERHUB_USERNAME/$SERVICE:latest"
  DOCKERFILE="$SERVICE.dockerfile"

  echo "=================================================="
  echo "Building and pushing $IMAGE_NAME"
  echo "Dockerfile: $DOCKERFILE"
  echo "Platforms:  linux/arm64"
  echo "=================================================="


  docker buildx build \
    --platform linux/arm64 \
    -t "$IMAGE_NAME" \
    -f "$DOCKERFILE" \
    --push \
    .

  if [ $? -ne 0 ]; then
    echo "Error: Docker buildx build/push failed for $SERVICE"
    exit 1
  fi

done

echo "All services have been built and pushed to Docker Hub."