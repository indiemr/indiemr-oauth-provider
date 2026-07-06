#!/usr/bin/env bash
set -euo pipefail

CONTAINER="${OPENMRS_CONTAINER:-openmrs-local}"
CONFIG_FILE="$(cd "$(dirname "$0")/.." && pwd)/config/application.yml"
REMOTE_DIR="/openmrs/data/indiemroauthprovider"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Missing $CONFIG_FILE" >&2
  exit 1
fi

docker exec "$CONTAINER" mkdir -p "$REMOTE_DIR"
docker cp "$CONFIG_FILE" "$CONTAINER:$REMOTE_DIR/application.yml"
echo "Injected config into $CONTAINER:$REMOTE_DIR/application.yml"
