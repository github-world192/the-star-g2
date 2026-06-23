#!/usr/bin/env bash
# One-time GCP VM firewall setup for Docker Hub deploy
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
PROJECT_ID="${GCP_PROJECT_ID:?Set GCP_PROJECT_ID}"

echo "==> Enabling Compute API"
gcloud services enable compute.googleapis.com --project="${PROJECT_ID}"

echo "==> Opening firewall port 80"
gcloud compute firewall-rules create "allow-${APP_NAME}" \
  --allow=tcp:80 \
  --target-tags="${APP_NAME}" \
  --description="Allow HTTP for ${APP_NAME}" \
  --project="${PROJECT_ID}" 2>/dev/null || echo "Firewall rule already exists"

echo ""
echo "Setup complete."
echo ""
echo "Tag your VM (replace INSTANCE and ZONE):"
echo "  gcloud compute instances add-tags INSTANCE --zone=ZONE --tags=${APP_NAME}"
echo ""
echo "Local machine:"
echo "  DOCKERHUB_USER=youruser ./scripts/docker-push.sh"
echo ""
echo "GCP VM (each deploy):"
echo "  export IMAGE=youruser/${APP_NAME}:latest"
echo "  bash /opt/${APP_NAME}/deploy.sh"