#!/usr/bin/env bash
# Pull image from Docker Hub and run on GCP VM
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
IMAGE="${IMAGE:?IMAGE is required, e.g. youruser/the-star-g2:latest}"
ENV_FILE="/opt/${APP_NAME}/.env"
CONTAINER_NAME="${APP_NAME}"
HOST_PORT="${HOST_PORT:-80}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE}. Create it before first deploy."
  exit 1
fi

echo "==> Pulling ${IMAGE}"
docker pull "${IMAGE}"

echo "==> Stopping old container"
docker stop "${CONTAINER_NAME}" 2>/dev/null || true
docker rm "${CONTAINER_NAME}" 2>/dev/null || true

echo "==> Starting new container"
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  -p "${HOST_PORT}:${CONTAINER_PORT}" \
  --env-file "${ENV_FILE}" \
  -e "PORT=${CONTAINER_PORT}" \
  "${IMAGE}"

echo "==> Deployment complete"
docker ps --filter "name=${CONTAINER_NAME}"