#!/usr/bin/env bash
# One-time setup on GCP Compute Engine VM (Debian/Ubuntu)
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
APP_DIR="/opt/${APP_NAME}"

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

echo "==> Creating app directory"
sudo mkdir -p "${APP_DIR}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
sudo cp "${SCRIPT_DIR}/deploy.sh" "${APP_DIR}/deploy.sh"
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

echo ""
echo "Bootstrap complete. Next steps:"
echo "  1. Edit ${APP_DIR}/.env with OAuth credentials"
echo "  2. Log out and back in (for docker group)"
echo "  3. Deploy an image from Docker Hub:"
echo "       export IMAGE=youruser/${APP_NAME}:latest"
echo "       bash ${APP_DIR}/deploy.sh"
echo ""
echo "  If the image is private, run 'docker login' on this VM first."