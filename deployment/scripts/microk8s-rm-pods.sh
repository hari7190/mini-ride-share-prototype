#!/usr/bin/env sh
# Delete pods whose container images match the five mini-ride-share app images.
# With Deployments, pods are recreated (useful after cache-busting image pulls).

set -eu

KUBECTL="${KUBECTL:-kubectl}"
# Match fragment as it appears in image refs (Helm uses this repository name).
IMAGE_MATCH="${IMAGE_MATCH:-mini-ride-share-platform}"

SERVICES="auth-service dispatch-engine driver-service location-tracker rider-service"
# Optional: restrict to one namespace, e.g. export NAMESPACE=default
NS_FLAG="${NAMESPACE:+-n ${NAMESPACE}}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required (sudo apt install jq / brew install jq)." >&2
  exit 1
fi

# Build regex: ...platform:(auth-service-latest|dispatch-engine-latest|...)
TAGS=""
for s in $SERVICES; do
  TAGS="${TAGS}${TAGS:+|}${s}-latest"
done
PATTERN="${IMAGE_MATCH}:(${TAGS})"

# List matching pods: namespace name
$KUBECTL get pods ${NS_FLAG:--A} -o json \
  | jq -r --arg re "$PATTERN" '
      .items[]
      | . as $pod
      | ($pod.spec.containers // []) + ($pod.spec.initContainers // [])
      | map(.image)
      | join(" ")
      | select(test($re))
      | "\($pod.metadata.namespace) \($pod.metadata.name)"
    ' \
  | sort -u \
  | while read -r ns name; do
        [ -z "${ns:-}" ] && continue
        echo "Deleting pod $ns/$name"
        $KUBECTL delete pod -n "$ns" "$name" --ignore-not-found "$@"
      done

echo "Done."
