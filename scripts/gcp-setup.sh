#!/usr/bin/env bash
# One-time GCP project setup (run locally with gcloud authenticated)
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
PROJECT_ID="${GCP_PROJECT_ID:?Set GCP_PROJECT_ID}"
REGION="${GCP_REGION:-asia-east1}"
SA_NAME="${SA_NAME:-github-deploy}"
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "==> Enabling required APIs"
gcloud services enable \
  artifactregistry.googleapis.com \
  compute.googleapis.com \
  iam.googleapis.com \
  --project="${PROJECT_ID}"

echo "==> Creating Artifact Registry repository"
gcloud artifacts repositories create "${APP_NAME}" \
  --repository-format=docker \
  --location="${REGION}" \
  --description="Docker images for ${APP_NAME}" \
  --project="${PROJECT_ID}" 2>/dev/null || echo "Repository already exists"

echo "==> Creating service account"
gcloud iam service-accounts create "${SA_NAME}" \
  --display-name="GitHub Actions deploy" \
  --project="${PROJECT_ID}" 2>/dev/null || echo "Service account already exists"

echo "==> Granting IAM roles"
for ROLE in \
  roles/artifactregistry.writer \
  roles/compute.viewer; do
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="${ROLE}" \
    --quiet
done

echo "==> Creating service account key"
KEY_FILE="${APP_NAME}-sa-key.json"
gcloud iam service-accounts keys create "${KEY_FILE}" \
  --iam-account="${SA_EMAIL}" \
  --project="${PROJECT_ID}"

if [[ -n "${VM_SA_EMAIL:-}" ]]; then
  echo "==> Granting VM service account pull access"
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${VM_SA_EMAIL}" \
    --role="roles/artifactregistry.reader" \
    --quiet
fi

echo ""
echo "Setup complete."
echo ""
echo "Add these GitHub Secrets (Settings → Secrets and variables → Actions):"
echo "  GCP_PROJECT_ID  = ${PROJECT_ID}"
echo "  GCP_REGION      = ${REGION}"
echo "  GCP_SA_KEY      = contents of ${KEY_FILE}"
echo "  GCE_HOST        = VM external IP"
echo "  GCE_USER        = SSH username on VM"
echo "  GCE_SSH_KEY     = private SSH key for VM access"
echo ""
echo "Grant VM pull access (replace with your VM service account):"
echo "  VM_SA_EMAIL=123456-compute@developer.gserviceaccount.com ./scripts/gcp-setup.sh"
echo ""
echo "Then run scripts/gcp-vm-bootstrap.sh on the VM."