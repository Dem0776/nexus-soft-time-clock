# 07 — Manual de despliegue en Portainer

Guía paso a paso para desplegar el **backend** (Spring Boot / Java 21) y la
**aplicación web** (Angular servida por NGINX) de Nexus Soft Time Clock usando
**Portainer** (Community o Business, edición standalone sobre Docker).

Complementa a [`02-guia-despliegue.md`](02-guia-despliegue.md). Para producción a
gran escala se recomienda Kubernetes; Portainer es ideal para entornos únicos,
demos, staging o instalaciones on-premise pequeñas/medianas.

---

## 1. Arquitectura del stack

Portainer levanta un **stack** (compose) con estos servicios en una red interna
privada. Solo NGINX expone un puerto al exterior:

```
                 ┌─────────── Portainer stack: nexus-time-clock ───────────┐
   Internet ──▶  │  nginx  ──▶  web (Angular/NGINX)   [ / ]                 │
   (puerto       │    │                                                     │
    HTTP_PORT)   │    └──────▶  backend (Spring Boot)  [ /api /ws /actuator]│
                 │                   │        │                             │
                 │                   ▼        ▼                             │
                 │               postgres    redis                         │
                 └──────────────────────────────────────────────────────────┘
```

- La web usa rutas **relativas** (`/api/v1`, `/ws`), por lo que **NGINX es
  obligatorio** como único punto de entrada: enruta `/` → web y
  `/api`, `/ws`, `/actuator`, `/swagger-ui` → backend.
- `postgres` (PostGIS) y `redis` quedan **solo en la red interna** (sin puertos
  publicados). El backend se conecta por nombre de servicio (`postgres`, `redis`).
- **Flyway** aplica las migraciones (`db/migration`) automáticamente al arrancar
  el backend; no hay paso manual de esquema.

> **MinIO (evidencias) y SMTP (notificaciones)** no se incluyen en el stack
> mínimo. Si tu iteración los requiere, añádelos como servicios extra o apunta el
> backend a instancias gestionadas mediante variables de entorno.

---

## 2. Requisitos previos

1. **Portainer** operativo y conectado a un entorno Docker (local o Agent).
2. En el host Docker: acceso a Internet para descargar imágenes base
   (`postgis/postgis`, `redis`, `nginx`, `maven`, `node`).
3. **Repositorio Git accesible** desde Portainer (recomendado — método A), o un
   **registry de imágenes** donde publicar backend/web (método B).
4. Recursos sugeridos del host: **≥ 4 vCPU, 8 GB RAM, 20 GB disco** (el build de
   Maven + Node es intensivo; ver §7 si el host es pequeño).

Los ficheros clave ya viven en el repo:

| Fichero | Rol |
|---|---|
| `infra/portainer-stack.yml` | Compose del stack (este manual lo usa) |
| `infra/backend.Dockerfile` | Build multi-stage del backend (JRE 21) |
| `infra/web.Dockerfile` | Build Angular → NGINX |
| `infra/nginx/nginx.conf` | Reverse proxy (rutas web/API/ws) |

---

## 3. Variables de entorno

Configúralas en Portainer (pestaña **Environment variables** del stack). Las
marcadas 🔴 son **obligatorias** y **sin valor por defecto** — el stack no
arranca sin ellas.

| Variable | Oblig. | Por defecto | Descripción |
|---|:---:|---|---|
| `DB_PASSWORD` | 🔴 | — | Contraseña de PostgreSQL. **Usar valor fuerte.** |
| `SECURITY_QR_SECRET` | 🔴 | — | Secreto HMAC para el QR firmado. **Rotar y guardar seguro.** |
| `DB_NAME` | | `nexus` | Nombre de la base de datos |
| `DB_USER` | | `nexus` | Usuario de PostgreSQL |
| `SPRING_PROFILES_ACTIVE` | | `prod` | Perfil de Spring |
| `HTTP_PORT` | | `8081` | Puerto que NGINX publica en el host |
| `SECURITY_JWT_ACCESS_TTL_SECONDS` | | `900` | TTL del access token |
| `SECURITY_JWT_REFRESH_TTL_DAYS` | | `30` | TTL del refresh token |
| `MAIL_HOST` / `MAIL_PORT` | | `mailhog` / `1025` | SMTP de notificaciones |
| `LOG_LEVEL_APP` | | `INFO` | Nivel de log de la app |
| `REGISTRY` / `TAG` | | — | Solo método B (registry) |

> ⚠️ **Genera secretos fuertes**, por ejemplo:
> `openssl rand -base64 32` para `SECURITY_QR_SECRET` y `DB_PASSWORD`.
> Nunca los subas al repositorio.

---

## 4. Método A — Desplegar desde el repositorio Git (recomendado)

Portainer clona el repo y **construye las imágenes** con los Dockerfiles. No
necesitas registry.

1. En Portainer: **Stacks → + Add stack**.
2. **Name**: `nexus-time-clock`.
3. **Build method**: selecciona **Repository**.
4. Rellena:
   - **Repository URL**: la URL de tu repositorio Git.
   - **Repository reference**: rama a desplegar (p. ej. `refs/heads/main`).
   - **Compose path**: `infra/portainer-stack.yml`.
   - **Authentication**: actívala si el repo es privado (usuario + token).
5. En **Environment variables**, añade al menos `DB_PASSWORD` y
   `SECURITY_QR_SECRET` (§3). Añade el resto según necesites.
6. (Opcional) Activa **GitOps updates / polling** para redesplegar
   automáticamente cuando cambie la rama.
7. Pulsa **Deploy the stack**.

Portainer ejecutará el build (Maven + Node → primera vez tarda varios minutos) y
levantará los contenedores. Sigue el progreso en **Stacks → nexus-time-clock**.

---

## 5. Método B — Imágenes pre-construidas en un registry

Recomendado para producción (builds reproducibles, arranque rápido, el host de
Portainer no compila).

### 5.1 Construir y publicar las imágenes

Desde una máquina con Docker y el repo clonado (o en tu CI):

```bash
# En la raíz del repositorio
REGISTRY=registry.midominio.com
TAG=1.0.0

docker build -f infra/backend.Dockerfile -t $REGISTRY/nexus/backend:$TAG .
docker build -f infra/web.Dockerfile     -t $REGISTRY/nexus/web:$TAG .

docker push $REGISTRY/nexus/backend:$TAG
docker push $REGISTRY/nexus/web:$TAG
```

> Tip: en `.github/workflows/ci.yml` puedes añadir un job que haga build+push en
> cada tag de release.

### 5.2 Ajustar el compose

En `infra/portainer-stack.yml`, en los servicios `backend` y `web`:
**comenta el bloque `build:`** y **descomenta la línea `image:`**.

### 5.3 Desplegar

1. **Stacks → + Add stack → Name**: `nexus-time-clock`.
2. **Build method**: **Web editor** y pega el contenido de
   `infra/portainer-stack.yml`, **o** usa **Repository** apuntando al compose.
3. En **Environment variables** añade además `REGISTRY` y `TAG`, y los secretos de §3.
4. Si el registry es privado: **Registries** en Portainer → añade credenciales
   antes de desplegar.
5. **Deploy the stack**.

> ⚠️ **NGINX y su config.** El servicio `nginx` monta `./nginx/nginx.conf`
> (bind-mount relativo al compose). Esto funciona con el método **Repository**
> (el repo está en el host). Con **Web editor puro** ese fichero no existe en el
> host: usa el método Repository, o baja `infra/nginx/nginx.conf` al host y ajusta
> la ruta del volumen, o hornea la config en una imagen NGINX propia.

---

## 6. Verificación post-despliegue

Con el stack **running** (5 contenedores en verde):

1. **Estado del backend** — desde el host o un contenedor de la red:
   ```bash
   curl http://localhost:${HTTP_PORT:-8081}/actuator/health
   # → {"status":"UP", ...}
   ```
2. **Portal web**: abre `http://<host>:<HTTP_PORT>/` en el navegador → carga la
   SPA de Angular.
3. **API vía proxy**: `http://<host>:<HTTP_PORT>/swagger-ui.html` muestra la
   documentación OpenAPI.
4. **Migraciones Flyway**: en los **logs del contenedor `backend`** debe verse
   `Successfully applied N migrations` sin errores.
5. **Base de datos**: en logs de `postgres`, `database system is ready to accept
   connections`.

Si algo falla, revisa **Logs** de cada contenedor desde Portainer
(Stacks → contenedor → Logs) y la §8.

---

## 7. Producción — recomendaciones

- **TLS obligatorio.** NGINX de este stack sirve HTTP en `HTTP_PORT`. Coloca
  delante un terminador TLS: un reverse proxy del host (Traefik, Caddy, NGINX del
  sistema con Let's Encrypt) o el balanceador de tu infraestructura. La app ya
  respeta `X-Forwarded-*` (`forward-headers-strategy: framework`).
- **Persistencia.** Los volúmenes `pgdata` y `redisdata` conservan los datos. En
  Portainer no marques *"prune"* al actualizar. Haz **backups** de `pgdata`
  (`pg_dump`) periódicamente.
- **Secretos.** Prefiere **Portainer Secrets** / gestor externo antes que texto
  plano en variables del stack para `DB_PASSWORD` y `SECURITY_QR_SECRET`.
- **Host pequeño.** El build de Maven+Node consume RAM. Si el host de Portainer es
  modesto, usa el **método B** (imágenes ya construidas en CI) para no compilar en
  el servidor.
- **SPA deep-linking.** Si al recargar una ruta profunda (p. ej. `/dashboard`)
  aparece un 404, la imagen `web` necesita fallback SPA
  (`try_files $uri /index.html`). Añádelo en `infra/web.Dockerfile` con una
  `nginx.conf` propia. La navegación normal dentro de la app no se ve afectada.
- **Actualizar el stack.** Método A: haz push a la rama y **Update the stack**
  (con *re-pull / re-build*). Método B: sube el `TAG`, y **Update** con *re-pull*.
  Flyway aplicará solo las migraciones nuevas (compatibles hacia atrás).

---

## 8. Solución de problemas

| Síntoma | Causa probable | Acción |
|---|---|---|
| Stack no arranca, error `falta DB_PASSWORD` | Variable obligatoria sin definir | Añade `DB_PASSWORD` y `SECURITY_QR_SECRET` en Environment variables |
| `backend` reinicia en bucle | Postgres/Redis aún no sanos, o credenciales BD mal | Revisa logs de `postgres`; verifica `DB_*`; espera al healthcheck |
| Web carga pero la API da 404/502 | NGINX no enruta o backend caído | Revisa logs de `nginx` y `backend`; confirma `/actuator/health` UP |
| 404 al recargar rutas del portal | Falta fallback SPA | Ver §7 (deep-linking) |
| `nginx` no arranca: no such file `nginx.conf` | Bind-mount sin fichero (web editor puro) | Usa método Repository o baja `nginx.conf` al host (ver §5.3) |
| Build falla por memoria | Host sin RAM para Maven/Node | Usa método B (registry) |
| Puerto en uso | `HTTP_PORT` ocupado en el host | Cambia `HTTP_PORT` a otro valor libre |

---

## Referencias

- Compose del stack: `infra/portainer-stack.yml`
- Dockerfiles: `infra/backend.Dockerfile`, `infra/web.Dockerfile`
- Reverse proxy: `infra/nginx/nginx.conf`
- Guía general de despliegue: [`02-guia-despliegue.md`](02-guia-despliegue.md)
- Topología de producción (K8s): [`../iteracion-02-arquitectura/08-despliegue.md`](../iteracion-02-arquitectura/08-despliegue.md)
