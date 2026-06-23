#!/usr/bin/env bash
# One-time GCP project setup for manual Docker push + VM pull deploy
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
PROJECT_ID="${GCP_PROJECT_ID:?Set GCP_PROJECT_ID}"
REGION="${GCP_REGION:-asia-east1}"

echo "==> Enabling required APIs"
gcloud services enable \
  artifactregistry.googleapis.com \
  compute.googleapis.com \
  --project="${PROJECT_ID}"

echo "==> Creating Artifact Registry repository"
gcloud artifacts repositories create "${APP_NAME}" \
  --repository-format=docker \
  --location="${REGION}" \
  --description="Docker images for ${APP_NAME}" \
  --project="${PROJECT_ID}" 2>/dev/null || echo "Repository already exists"

if [[ -n "${VM_SA_EMAIL:-}" ]]; then
  echo "==> Granting VM service account pull access"
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${VM_SA_EMAIL}" \
    --role="roles/artifactregistry.reader" \
    --quiet
else
  echo ""
  echo "Tip: grant your VM's service account pull access:"
  echo "  VM_SA_EMAIL=123456-compute@developer.gserviceaccount.com ./scripts/gcp-setup.sh"
fi

echo ""
echo "Setup complete."
echo ""
echo "Local machine (needs gcloud auth + docker):"
echo "  GCP_PROJECT_ID=${PROJECT_ID} GCP_REGION=${REGION} ./scripts/docker-push.sh"
echo ""
echo "GCP VM (one-time bootstrap):"
echo "  GCP_REGION=${REGION} ./scripts/gcp-vm-bootstrap.sh"
echo "  sudo nano /opt/${APP_NAME}/.env"
echo ""
echo "GCP VM (each deploy):"
echo "  export IMAGE=${REGION}-docker.pkg.dev/${PROJECT_ID}/${APP_NAME}:latest"
echo "  export GCP_REGION=${REGION}"
echo "  bash /opt/${APP_NAME}/deploy.sh"