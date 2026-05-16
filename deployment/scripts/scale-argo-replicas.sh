#!/usr/bin/env bash
# Set replicaCount overrides in Argo CD Application manifests (deployment/argo-apps/*-app.yaml).
# Does not apply to the cluster — run kubectl apply or argocd app sync yourself.

set -euo pipefail

MIN_REPLICAS=0
MAX_REPLICAS=2

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ARGO_APPS_DIR="$(cd "${SCRIPT_DIR}/../argo-apps" && pwd)"

usage() {
  cat <<EOF
Usage: $(basename "$0") [REPLICA_COUNT]

Update helm.parameters replicaCount in all Argo Application YAMLs under:
  ${ARGO_APPS_DIR}

  REPLICA_COUNT  Integer from ${MIN_REPLICAS} to ${MAX_REPLICAS} (optional; prompts if omitted)

Examples:
  $(basename "$0")       # interactive prompt
  $(basename "$0") 0     # scale down
  $(basename "$0") 2     # scale up

After editing, apply when ready:
  kubectl apply -f ${ARGO_APPS_DIR}/
  # or: argocd app sync -l ...
EOF
}

validate_replicas() {
  local count="$1"
  if ! [[ "$count" =~ ^[0-9]+$ ]]; then
    echo "Error: replica count must be an integer (got: ${count})." >&2
    exit 1
  fi
  if (( count < MIN_REPLICAS || count > MAX_REPLICAS )); then
    echo "Error: replica count must be between ${MIN_REPLICAS} and ${MAX_REPLICAS} (got: ${count})." >&2
    exit 1
  fi
}

read_replica_count() {
  local input
  printf "Replica count (%s-%s): " "$MIN_REPLICAS" "$MAX_REPLICAS"
  read -r input
  if [[ -z "${input:-}" ]]; then
    echo "Error: no value entered." >&2
    exit 1
  fi
  REPLICAS="$input"
}

update_app_file() {
  local file="$1"
  local replicas="$2"
  local tmp
  tmp="$(mktemp)"

  awk -v replicas="$replicas" '
    /name: replicaCount/ { seen = 1 }
    seen && /^[[:space:]]+value:/ {
      sub(/value:.*/, "value: \"" replicas "\"")
      seen = 0
    }
    { print }
  ' "$file" > "$tmp"

  if ! grep -q 'name: replicaCount' "$tmp"; then
    rm -f "$tmp"
    echo "Error: ${file} has no helm.parameters replicaCount block." >&2
    exit 1
  fi

  if ! grep -q "value: \"${replicas}\"" "$tmp"; then
    rm -f "$tmp"
    echo "Error: failed to update replicaCount in ${file}." >&2
    exit 1
  fi

  mv "$tmp" "$file"
}

show_current() {
  local file app replicas
  printf "\n%-28s %s\n" "APPLICATION" "replicaCount"
  printf "%-28s %s\n" "-----------" "-------------"
  for file in "${ARGO_APPS_DIR}"/*-app.yaml; do
    [[ -f "$file" ]] || continue
    app="$(awk '/^  name: / { print $2; exit }' "$file")"
    replicas="$(awk '
      /name: replicaCount/ { seen = 1; next }
      seen && /^[[:space:]]+value:/ {
        gsub(/.*value:[[:space:]]*"?|"?[[:space:]]*$/, "")
        print
        exit
      }
    ' "$file")"
    printf "%-28s %s\n" "${app:-$(basename "$file")}" "${replicas:-?}"
  done
  printf "\n"
}

main() {
  if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
  fi

  if [[ -n "${1:-}" ]]; then
    REPLICAS="$1"
  else
    read_replica_count
  fi

  validate_replicas "$REPLICAS"

  shopt -s nullglob
  local files=("${ARGO_APPS_DIR}"/*-app.yaml)
  if (( ${#files[@]} == 0 )); then
    echo "Error: no *-app.yaml files found in ${ARGO_APPS_DIR}." >&2
    exit 1
  fi

  echo "Setting replicaCount to ${REPLICAS} in ${#files[@]} Argo Application manifest(s)..."
  for file in "${files[@]}"; do
    echo "  $(basename "$file")"
    update_app_file "$file" "$REPLICAS"
  done

  show_current
  echo "Done. Files updated only — not applied to the cluster."
}

main "$@"
