#!/usr/bin/env bash
# Build locally and push to GCP Artifact Registry
set -euo pipefail

APP_NAME="${APP_NAME:-the-star-g2}"
GCP_PROJECT_ID="${GCP_PROJECT_ID:?Set GCP_PROJECT_ID}"
GCP_REGION="${GCP_REGION:-asia-east1}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

REGISTRY="${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${APP_NAME}"
IMAGE="${REGISTRY}/${APP_NAME}:${IMAGE_TAG}"

echo "==> Building ${IMAGE}"
docker build -t "${IMAGE}" .

echo "==> Authenticating Docker with Artifact Registry"
gcloud auth configure-docker "${GCP_REGION}-docker.pkg.dev" --quiet

echo "==> Pushing ${IMAGE}"
docker push "${IMAGE}"

echo ""
echo "Push complete."
echo "  IMAGE=${IMAGE}"
echo ""
echo "On GCP VM, deploy with:"
echo "  export IMAGE=${IMAGE}"
echo "  export GCP_REGION=${GCP_REGION}"
echo "  bash /opt/${APP_NAME}/deploy.sh"