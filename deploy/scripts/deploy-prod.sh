#!/usr/bin/env bash
set -eu

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <docker-image>"
  exit 1
fi

DOCKER_IMAGE_INPUT="$1"
APP_DIR="${APP_DIR:-$(pwd)}"
ENV_FILE="$APP_DIR/deploy/.env.prod"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"
CURRENT_VERSION_FILE="$APP_DIR/deploy/.current-version"

required_envs="APP_ENV POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD JWT_SECRET_KEY"
for env_name in $required_envs; do
  if [ -z "${!env_name:-}" ]; then
    echo "Missing required environment variable: $env_name"
    exit 1
  fi
done

mkdir -p "$APP_DIR/deploy"

cat >"$ENV_FILE" <<EOF
APP_ENV=${APP_ENV}
POSTGRES_DB=${POSTGRES_DB}
POSTGRES_USER=${POSTGRES_USER}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
JWT_SECRET_KEY=${JWT_SECRET_KEY}
DOCKER_IMAGE=${DOCKER_IMAGE_INPUT}
EOF

docker compose version
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down --remove-orphans || true
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" pull backend
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

"$APP_DIR/deploy/scripts/post-deploy-healthcheck.sh" "$COMPOSE_FILE" "$ENV_FILE"

printf '%s\n' "$DOCKER_IMAGE_INPUT" > "$CURRENT_VERSION_FILE"
