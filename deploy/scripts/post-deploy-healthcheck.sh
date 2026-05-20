#!/usr/bin/env bash
set -eu

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <compose-file> <env-file>"
  exit 1
fi

COMPOSE_FILE="$1"
ENV_FILE="$2"

docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps

BACKEND_CONTAINER_ID="$(docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps -q backend)"
PROXY_CONTAINER_ID="$(docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps -q proxy)"
DB_CONTAINER_ID="$(docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps -q db)"

if [ -z "$BACKEND_CONTAINER_ID" ] || [ -z "$PROXY_CONTAINER_ID" ] || [ -z "$DB_CONTAINER_ID" ]; then
  echo "One or more containers are missing"
  exit 1
fi

backend_running="$(docker inspect -f '{{.State.Running}}' "$BACKEND_CONTAINER_ID")"
proxy_running="$(docker inspect -f '{{.State.Running}}' "$PROXY_CONTAINER_ID")"
db_running="$(docker inspect -f '{{.State.Running}}' "$DB_CONTAINER_ID")"

if [ "$backend_running" != "true" ] || [ "$proxy_running" != "true" ] || [ "$db_running" != "true" ]; then
  echo "One or more containers are not running"
  exit 1
fi

for _ in $(seq 1 30); do
  if curl -fsS http://127.0.0.1/actuator/health >/tmp/json-backend-health.json; then
    break
  fi
  sleep 5
done

curl -fsS http://127.0.0.1/actuator/health >/tmp/json-backend-health.json
curl -fsS http://127.0.0.1/api/products >/tmp/json-backend-products.json

grep -q '"status"' /tmp/json-backend-health.json
