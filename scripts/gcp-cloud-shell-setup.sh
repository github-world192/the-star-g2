#!/usr/bin/env bash
# Run in GCP Cloud Shell for one-time project + VM setup
set -euo pipefail

export GCP_PROJECT_ID="${GCP_PROJECT_ID:-project-3590b91c-e095-4141-932}"
export GCP_REGION="${GCP_REGION:-us-central1}"
export GCE_HOST="${GCE_HOST:-34.173.119.243}"
export APP_NAME="${APP_NAME:-the-star-g2}"

gcloud config set project "${GCP_PROJECT_ID}"

echo "==> Enable APIs"
gcloud services enable artifactregistry.googleapis.com compute.googleapis.com

echo "==> Create Artifact Registry"
gcloud artifacts repositories create "${APP_NAME}" \
  --repository-format=docker \
  --location="${GCP_REGION}" \
  --description="Docker images for ${APP_NAME}" 2>/dev/null || true

INSTANCE=$(gcloud compute instances list \
  --filter="networkInterfaces.accessConfigs.natIP=${GCE_HOST}" \
  --format="value(name,zone,serviceAccounts[0].email)" | head -1)

if [[ -n "${INSTANCE}" ]]; then
  read -r VM_NAME VM_ZONE VM_SA <<< "${INSTANCE}"
  echo "==> Found VM: ${VM_NAME} (${VM_ZONE}), SA: ${VM_SA}"
  gcloud projects add-iam-policy-binding "${GCP_PROJECT_ID}" \
    --member="serviceAccount:${VM_SA}" \
    --role="roles/artifactregistry.reader" --quiet
else
  echo "WARN: VM with IP ${GCE_HOST} not found. Grant artifactregistry.reader to VM SA manually."
fi

echo "==> Open firewall port 80"
gcloud compute firewall-rules create "allow-${APP_NAME}" \
  --allow=tcp:80 \
  --target-tags="${APP_NAME}" \
  --description="Allow HTTP for ${APP_NAME}" 2>/dev/null || true

if [[ -n "${VM_NAME:-}" && -n "${VM_ZONE:-}" ]]; then
  gcloud compute instances add-tags "${VM_NAME}" --zone="${VM_ZONE}" --tags="${APP_NAME}" 2>/dev/null || true
fi

cat <<EOF

Done. Deploy flow (no CI/CD):

1. On your laptop (with gcloud + docker):
   GCP_PROJECT_ID=${GCP_PROJECT_ID} GCP_REGION=${GCP_REGION} ./scripts/docker-push.sh

2. SSH into VM (${GCE_HOST}), one-time bootstrap:
   git clone https://github.com/github-world192/the-star-g2.git
   cd the-star-g2 && GCP_REGION=${GCP_REGION} ./scripts/gcp-vm-bootstrap.sh
   sudo nano /opt/${APP_NAME}/.env   # OAuth credentials

3. On VM, pull and run:
   export IMAGE=${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${APP_NAME}:latest
   export GCP_REGION=${GCP_REGION}
   bash /opt/${APP_NAME}/deploy.sh

4. Google OAuth redirect URI:
   http://${GCE_HOST}/callback

EOF