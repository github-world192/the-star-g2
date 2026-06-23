#!/usr/bin/env bash
# Run in GCP Cloud Shell for one-time VM firewall setup
set -euo pipefail

export GCP_PROJECT_ID="${GCP_PROJECT_ID:-project-3590b91c-e095-4141-932}"
export GCE_HOST="${GCE_HOST:-34.173.119.243}"
export APP_NAME="${APP_NAME:-the-star-g2}"
export DOCKERHUB_USER="${DOCKERHUB_USER:-your-dockerhub-username}"

gcloud config set project "${GCP_PROJECT_ID}"

echo "==> Enable Compute API"
gcloud services enable compute.googleapis.com

echo "==> Open firewall port 80"
gcloud compute firewall-rules create "allow-${APP_NAME}" \
  --allow=tcp:80 \
  --target-tags="${APP_NAME}" \
  --description="Allow HTTP for ${APP_NAME}" 2>/dev/null || true

INSTANCE=$(gcloud compute instances list \
  --filter="networkInterfaces.accessConfigs.natIP=${GCE_HOST}" \
  --format="value(name,zone)" | head -1)

if [[ -n "${INSTANCE}" ]]; then
  read -r VM_NAME VM_ZONE <<< "${INSTANCE}"
  echo "==> Found VM: ${VM_NAME} (${VM_ZONE})"
  gcloud compute instances add-tags "${VM_NAME}" --zone="${VM_ZONE}" --tags="${APP_NAME}" 2>/dev/null || true
else
  echo "WARN: VM with IP ${GCE_HOST} not found."
fi

cat <<EOF

Done. Deploy flow (Docker Hub):

1. On your laptop:
   DOCKERHUB_USER=${DOCKERHUB_USER} ./scripts/docker-push.sh

2. SSH into VM (${GCE_HOST}), one-time bootstrap:
   git clone https://github.com/github-world192/the-star-g2.git
   cd the-star-g2 && ./scripts/gcp-vm-bootstrap.sh
   sudo nano /opt/${APP_NAME}/.env   # OAuth credentials

3. On VM, pull and run:
   export IMAGE=${DOCKERHUB_USER}/${APP_NAME}:latest
   bash /opt/${APP_NAME}/deploy.sh

4. Google OAuth redirect URI:
   http://${GCE_HOST}/callback

EOF