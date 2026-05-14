#!/usr/bin/env sh
# Remove cached MicroK8s (containerd) images for all app services under services/.
# Matches Helm values: docker.io/dockerprogram/mini-ride-share-platform:<name>-latest
#
# Usage: ./microk8s-rm-platform-images.sh
# Requires: microk8s (and permission to run `microk8s ctr`; use sudo if your cluster does).

set -eu

# --- edit if your registry/repo/tag scheme changes ---
IMAGE_REPO="${IMAGE_REPO:-docker.io/dockerprogram/mini-ride-share-platform}"

# Directory names under services/ (must match Helm image tags: <name>-latest)
SERVICES="auth-service dispatch-engine driver-service location-tracker rider-service"

for name in $SERVICES; do
  tag="${name}-latest"
  full="${IMAGE_REPO}:${tag}"
  echo "=== ${full} ==="
  microk8s ctr images ls | grep -F "${name}" || true
  microk8s ctr images rm "${full}" || echo "rm skipped or failed (not present / in use): ${full}"
done

echo "Done."
