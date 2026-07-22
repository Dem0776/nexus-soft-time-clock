#!/usr/bin/env bash
# =====================================================================
# redeploy.sh — Redespliegue del stack Nexus Soft Time Clock en Portainer
# (método Git). Autentica, dispara "git pull + redeploy" reenviando las
# env-vars, y verifica salud + login tras el arranque.
#
# Uso:
#   1) cp scripts/.env.example scripts/.env  &&  editar scripts/.env
#   2) bash scripts/redeploy.sh
#
# Los secretos se leen de scripts/.env o del entorno. NADA hardcodeado.
# Flags:
#   --pull-image     fuerza re-pull/reconstrucción total de imágenes
#   --no-verify      no ejecuta la verificación post-deploy
#   -h | --help      ayuda
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PULL_IMAGE=false
DO_VERIFY=true

for arg in "$@"; do
  case "$arg" in
    --pull-image) PULL_IMAGE=true ;;
    --no-verify)  DO_VERIFY=false ;;
    -h|--help)
      cat <<'HELP'
redeploy.sh — Redespliegue del stack Nexus Soft Time Clock en Portainer (método Git).
Autentica, dispara "git pull + redeploy" reenviando las env-vars, y verifica
salud + login tras el arranque.

Uso:
  1) cp scripts/.env.example scripts/.env   &&   editar scripts/.env
  2) bash scripts/redeploy.sh [flags]

Los secretos se leen de scripts/.env o del entorno. NADA hardcodeado.

Flags:
  --pull-image   fuerza re-pull/reconstrucción total de imágenes
  --no-verify    no ejecuta la verificación post-deploy
  -h | --help    esta ayuda
HELP
      exit 0 ;;
    *) echo "Argumento desconocido: $arg" >&2; exit 2 ;;
  esac
done

# --- Cargar config local si existe -----------------------------------
if [[ -f "$SCRIPT_DIR/.env" ]]; then
  # shellcheck disable=SC1091
  set -a; source "$SCRIPT_DIR/.env"; set +a
fi

# --- Defaults --------------------------------------------------------
PORTAINER_URL="${PORTAINER_URL:-https://85.239.240.43:9443}"
PORTAINER_USER="${PORTAINER_USER:-Admon_dev}"
STACK_ID="${STACK_ID:-72}"
ENDPOINT_ID="${ENDPOINT_ID:-2}"
GH_USER="${GH_USER:-Dem0776}"
GIT_REF="${GIT_REF:-refs/heads/main}"
DB_NAME="${DB_NAME:-nexus}"
DB_USER="${DB_USER:-nexus}"
HTTP_PORT="${HTTP_PORT:-8088}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
LOG_LEVEL_APP="${LOG_LEVEL_APP:-INFO}"
APP_URL="${APP_URL:-http://85.239.240.43:8088}"
VERIFY_EMAIL="${VERIFY_EMAIL:-admin@demo.com}"
VERIFY_PASSWORD="${VERIFY_PASSWORD:-Admin123!}"

# --- Validar secretos obligatorios -----------------------------------
missing=()
[[ -z "${PORTAINER_PASS:-}" ]]      && missing+=("PORTAINER_PASS")
[[ -z "${GH_PAT:-}" ]]              && missing+=("GH_PAT")
[[ -z "${DB_PASSWORD:-}" ]]         && missing+=("DB_PASSWORD")
[[ -z "${SECURITY_QR_SECRET:-}" ]] && missing+=("SECURITY_QR_SECRET")
if (( ${#missing[@]} )); then
  echo "ERROR: faltan variables obligatorias: ${missing[*]}" >&2
  echo "       Defínelas en $SCRIPT_DIR/.env (ver .env.example) o expórtalas." >&2
  exit 1
fi

need() { command -v "$1" >/dev/null 2>&1 || { echo "ERROR: falta '$1' en el PATH" >&2; exit 1; }; }
need curl

# JSON-escape de un string (para valores con caracteres especiales)
json_escape() {
  local s=$1
  s=${s//\\/\\\\}; s=${s//\"/\\\"}
  printf '%s' "$s"
}

log() { printf '\033[1;34m›\033[0m %s\n' "$*"; }
ok()  { printf '\033[1;32m✔\033[0m %s\n' "$*"; }
err() { printf '\033[1;31m✖\033[0m %s\n' "$*" >&2; }

# --- 1) Autenticación ------------------------------------------------
log "Autenticando en Portainer ($PORTAINER_URL) como $PORTAINER_USER…"
JWT=$(curl -sk --fail -m 20 -X POST "$PORTAINER_URL/api/auth" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$(json_escape "$PORTAINER_USER")\",\"password\":\"$(json_escape "$PORTAINER_PASS")\"}" \
  | sed -E 's/.*"jwt":"([^"]+)".*/\1/') || { err "Fallo de autenticación (¿usuario/contraseña?)."; exit 1; }
[[ -n "$JWT" && "$JWT" != *"{"* ]] || { err "No se obtuvo JWT."; exit 1; }
ok "Autenticado."

# --- 2) Redespliegue -------------------------------------------------
log "Redesplegando stack Id $STACK_ID (ref $GIT_REF, pullImage=$PULL_IMAGE)…"
PAYLOAD=$(cat <<JSON
{
  "repositoryAuthentication": true,
  "repositoryUsername": "$(json_escape "$GH_USER")",
  "repositoryPassword": "$(json_escape "$GH_PAT")",
  "repositoryReferenceName": "$(json_escape "$GIT_REF")",
  "pullImage": $PULL_IMAGE,
  "prune": false,
  "env": [
    {"name":"SPRING_PROFILES_ACTIVE","value":"$(json_escape "$SPRING_PROFILES_ACTIVE")"},
    {"name":"DB_NAME","value":"$(json_escape "$DB_NAME")"},
    {"name":"DB_USER","value":"$(json_escape "$DB_USER")"},
    {"name":"DB_PASSWORD","value":"$(json_escape "$DB_PASSWORD")"},
    {"name":"SECURITY_QR_SECRET","value":"$(json_escape "$SECURITY_QR_SECRET")"},
    {"name":"HTTP_PORT","value":"$(json_escape "$HTTP_PORT")"},
    {"name":"LOG_LEVEL_APP","value":"$(json_escape "$LOG_LEVEL_APP")"}
  ]
}
JSON
)
HTTP=$(curl -sk -m 900 -o /tmp/redeploy_resp.$$ -w '%{http_code}' \
  -X PUT "$PORTAINER_URL/api/stacks/$STACK_ID/git/redeploy?endpointId=$ENDPOINT_ID" \
  -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" \
  -d "$PAYLOAD") || true
if [[ "$HTTP" != "200" ]]; then
  err "Redeploy falló (HTTP $HTTP):"; cat "/tmp/redeploy_resp.$$" >&2; echo >&2
  rm -f "/tmp/redeploy_resp.$$"; exit 1
fi
rm -f "/tmp/redeploy_resp.$$"
ok "Redeploy aceptado (HTTP 200). Portainer está reconstruyendo/levantando."

# --- 3) Verificación -------------------------------------------------
if [[ "$DO_VERIFY" != true ]]; then
  ok "Listo (verificación omitida por --no-verify)."
  exit 0
fi

log "Esperando a que el backend arranque (health UP)… hasta ~120 s"
UP=false
for i in $(seq 1 20); do
  H=$(curl -s -m 8 "$APP_URL/actuator/health" 2>/dev/null || true)
  if printf '%s' "$H" | grep -q '"status":"UP"'; then UP=true; ok "Health UP (~$((i*6)) s)."; break; fi
  sleep 6
done
if [[ "$UP" != true ]]; then
  err "El backend no reportó UP a tiempo. Revisa los logs del contenedor 'backend' en Portainer."
  exit 1
fi

log "Probando login de $VERIFY_EMAIL…"
CODE=$(curl -s -m 12 -o /dev/null -w '%{http_code}' -X POST "$APP_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$(json_escape "$VERIFY_EMAIL")\",\"password\":\"$(json_escape "$VERIFY_PASSWORD")\"}")
[[ "$CODE" == "200" ]] && ok "Login OK (200)." || err "Login devolvió HTTP $CODE (¿usuario sembrado?)."

log "Probando enlace directo del portal (/login)…"
CODE=$(curl -s -m 10 -o /dev/null -w '%{http_code}' "$APP_URL/login")
[[ "$CODE" == "200" ]] && ok "Portal/deep-link OK (200)." || err "/login devolvió HTTP $CODE."

echo
ok "Redespliegue completado. App: $APP_URL/login"
