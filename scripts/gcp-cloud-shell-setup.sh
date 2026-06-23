#!/usr/bin/env bash
# Run this in GCP Cloud Shell (has Owner access to the project)
set -euo pipefail

export GCP_PROJECT_ID="${GCP_PROJECT_ID:-project-3590b91c-e095-4141-932}"
export GCP_REGION="${GCP_REGION:-us-central1}"
export GCE_HOST="${GCE_HOST:-34.173.119.243}"
export APP_NAME="${APP_NAME:-the-star-g2}"
export SA_NAME="${SA_NAME:-github-deploy}"
SA_EMAIL="${SA_NAME}@${GCP_PROJECT_ID}.iam.gserviceaccount.com"

gcloud config set project "${GCP_PROJECT_ID}"

echo "==> Enable APIs"
gcloud services enable artifactregistry.googleapis.com compute.googleapis.com iam.googleapis.com

echo "==> Create Artifact Registry"
gcloud artifacts repositories create "${APP_NAME}" \
  --repository-format=docker \
  --location="${GCP_REGION}" \
  --description="Docker images for ${APP_NAME}" 2>/dev/null || true

echo "==> Create GitHub deploy service account"
gcloud iam service-accounts create "${SA_NAME}" \
  --display-name="GitHub Actions deploy" 2>/dev/null || true

for ROLE in roles/artifactregistry.writer roles/compute.viewer; do
  gcloud projects add-iam-policy-binding "${GCP_PROJECT_ID}" \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="${ROLE}" --quiet
done

echo "==> Create SA key (download this for GitHub secret GCP_SA_KEY)"
gcloud iam service-accounts keys create "${APP_NAME}-sa-key.json" \
  --iam-account="${SA_EMAIL}"

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

Done. Next:

1. GitHub → the-star-g2 → Settings → Secrets → Actions:
   GCP_SA_KEY = paste contents of ${APP_NAME}-sa-key.json
   GCE_SSH_KEY = your SSH private key for the VM

2. GitHub → Settings → Variables → Actions (if not set):
   GCP_PROJECT_ID = ${GCP_PROJECT_ID}
   GCP_REGION     = ${GCP_REGION}
   GCE_HOST       = ${GCE_HOST}
   GCE_USER       = your VM SSH username

3. SSH into VM (${GCE_HOST}) and run:
   git clone https://github.com/github-world192/the-star-g2.git
   cd the-star-g2 && GCP_REGION=${GCP_REGION} ./scripts/gcp-vm-bootstrap.sh
   sudo nano /opt/the-star-g2/.env   # add OAuth credentials

4. Google OAuth redirect URI:
   http://${GCE_HOST}/callback

EOF