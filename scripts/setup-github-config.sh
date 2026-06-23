#!/usr/bin/env bash
set -euo pipefail

REPO="${REPO:-github-world192/the-star-g2}"
GCP_PROJECT_ID="${GCP_PROJECT_ID:-project-3590b91c-e095-4141-932}"
GCP_REGION="${GCP_REGION:-us-central1}"
GCE_HOST="${GCE_HOST:-34.173.119.243}"
GCE_USER="${GCE_USER:-ubuntu}"

echo "==> Setting GitHub repository variables"
gh variable set GCP_PROJECT_ID --body "${GCP_PROJECT_ID}" --repo "${REPO}"
gh variable set GCP_REGION --body "${GCP_REGION}" --repo "${REPO}"
gh variable set GCE_HOST --body "${GCE_HOST}" --repo "${REPO}"
gh variable set GCE_USER --body "${GCE_USER}" --repo "${REPO}"

if [[ -f "${HOME}/.ssh/id_rsa" ]]; then
  echo "==> Setting GCE_SSH_KEY secret from ~/.ssh/id_rsa"
  gh secret set GCE_SSH_KEY < "${HOME}/.ssh/id_rsa" --repo "${REPO}"
else
  echo "Skip GCE_SSH_KEY: ~/.ssh/id_rsa not found"
fi

if [[ -f "the-star-g2-sa-key.json" ]]; then
  echo "==> Setting GCP_SA_KEY secret"
  gh secret set GCP_SA_KEY < "the-star-g2-sa-key.json" --repo "${REPO}"
else
  echo "Skip GCP_SA_KEY: run gcp-cloud-shell-setup.sh first"
fi

echo "Done. Re-run workflow: gh workflow run deploy-gcp.yml --repo ${REPO}"