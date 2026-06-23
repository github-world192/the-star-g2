#!/usr/bin/env bash
# One-time setup on GCP Compute Engine VM (Debian/Ubuntu)
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
APP_DIR="/opt/${APP_NAME}"
GCP_REGION="${GCP_REGION:?Set GCP_REGION, e.g. asia-east1}"

echo "==> Installing Docker"
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "${USER}"

echo "==> Installing Google Cloud CLI (if missing)"
if ! command -v gcloud >/dev/null 2>&1; then
  curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
  echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | \
    sudo tee /etc/apt/sources.list.d/google-cloud-sdk.list
  sudo apt-get update
  sudo apt-get install -y google-cloud-cli
fi

echo "==> Creating app directory"
sudo mkdir -p "${APP_DIR}"
sudo cp "$(dirname "$0")/deploy.sh" "${APP_DIR}/deploy.sh"
sudo chmod +x "${APP_DIR}/deploy.sh"
sudo chown -R "${USER}:${USER}" "${APP_DIR}"

if [[ ! -f "${APP_DIR}/.env" ]]; then
  cat > "${APP_DIR}/.env" <<'EOF'
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
PORT=8080
EOF
  echo "Created ${APP_DIR}/.env — edit with real OAuth credentials."
fi

echo "==> Configuring Docker auth for Artifact Registry"
gcloud auth configure-docker "${GCP_REGION}-docker.pkg.dev" --quiet

echo ""
echo "Bootstrap complete. Next steps:"
echo "  1. Edit ${APP_DIR}/.env with OAuth credentials"
echo "  2. Log out and back in (for docker group)"
echo "  3. Open firewall: gcloud compute firewall-rules create allow-${APP_NAME} --allow tcp:80 --target-tags=${APP_NAME}"
echo "  4. Tag VM: gcloud compute instances add-tags INSTANCE_NAME --tags=${APP_NAME} --zone=ZONE"