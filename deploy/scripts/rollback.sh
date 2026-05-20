#!/usr/bin/env bash
set -eu

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <docker-image>"
  exit 1
fi

DOCKER_IMAGE_INPUT="$1"
APP_DIR="${APP_DIR:-$(pwd)}"

"$APP_DIR/deploy/scripts/deploy-prod.sh" "$DOCKER_IMAGE_INPUT"
