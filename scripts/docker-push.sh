#!/usr/bin/env bash
# Build locally and push to Docker Hub
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
DOCKERHUB_USER="${DOCKERHUB_USER:?Set DOCKERHUB_USER}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

IMAGE="${DOCKERHUB_USER}/${APP_NAME}:${IMAGE_TAG}"

echo "==> Building ${IMAGE}"
docker build -t "${IMAGE}" .

if [[ -n "${DOCKERHUB_TOKEN:-}" ]]; then
  echo "==> Logging in to Docker Hub"
  echo "${DOCKERHUB_TOKEN}" | docker login -u "${DOCKERHUB_USER}" --password-stdin
else
  echo "==> Logging in to Docker Hub (interactive)"
  docker login -u "${DOCKERHUB_USER}"
fi

echo "==> Pushing ${IMAGE}"
docker push "${IMAGE}"

echo ""
echo "Push complete."
echo "  IMAGE=${IMAGE}"
echo ""
echo "On GCP VM, deploy with:"
echo "  export IMAGE=${IMAGE}"
echo "  bash /opt/${APP_NAME}/deploy.sh"