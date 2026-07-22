# Iteración 4 — Configuración del repositorio y estructura de proyectos

**Objetivo:** dejar el monorepo inicializado y los tres proyectos (backend, web, mobile) con un **esqueleto compilable**, más la infraestructura de desarrollo (docker-compose) y CI, listos para recibir los features de las siguientes iteraciones.

## Estructura del monorepo

```
nexus-soft-time-clock/
├── backend/                 # Spring Boot — modular monolith (Maven multi-módulo)
│   ├── pom.xml              # parent (Java 21, Spring Boot 3.3.4, BOM interno)
│   ├── shared-kernel/       # tipos de dominio comunes (AggregateRoot, DomainEvent, TenantId)
│   ├── platform/            # transversal: TenantContext, manejo uniforme de errores
│   └── bootstrap/           # app ejecutable: Flyway, seguridad stub, OpenAPI, /api/v1/ping
├── web/                     # Angular 18 (standalone, Signals, Material, PWA)
├── mobile/                  # Flutter (feature-first, Riverpod, GoRouter, Material 3, i18n)
├── infra/                   # docker-compose, Dockerfiles, nginx, prometheus, .env.example
├── db/migration/            # migraciones Flyway V1–V10 (fuente única; copiadas al backend en build)
├── docs/                    # documentación por iteración
└── .github/workflows/ci.yml # CI: backend (JDK21+Maven), web (Node+Angular), mobile (Flutter)
```

## Cómo ejecutar (desarrollo)

```bash
# 1) Infra (PostGIS, Redis, MinIO, MailHog, Prometheus, Grafana) + backend + web
docker compose -f infra/docker-compose.yml up -d --build

# 2) Solo dependencias de datos, y backend/web en local:
docker compose -f infra/docker-compose.yml up -d postgres redis minio mailhog
cd backend && mvn spring-boot:run -pl bootstrap            # requiere JDK 21
cd web && npm install && npm start                         # http://localhost:4200
cd mobile && flutter pub get && flutter run                # requiere Flutter SDK
```

Endpoints de verificación del backend: `GET /api/v1/ping`, `/actuator/health`, `/swagger-ui.html`, `/actuator/prometheus`.

## Estado de verificación (honesto)

| Proyecto | Verificación | Resultado |
|---|---|---|
| **web (Angular)** | `npm install` + `npm run build` (producción) | ✅ **Compila** (bundle generado, lazy loading, PWA) |
| **backend (Maven)** | `mvn validate` (modelo de proyecto) | ✅ POMs válidos y resolubles |
| **backend (compilación)** | `mvn compile` con **JDK 21** | ⚠️ **No ejecutado** — el entorno tiene JDK 17; target es Java 21 |
| **backend (tests)** | Testcontainers (PostGIS) | ⚠️ **No ejecutado** — requiere Docker activo + JDK 21 |
| **mobile (Flutter)** | `flutter analyze` / `flutter test` | ⚠️ **No ejecutado** — Flutter SDK no instalado |
| **infra (compose)** | `docker compose up` | ⚠️ **No ejecutado** — daemon Docker inactivo |

> Lo verificable con el toolchain disponible (Angular) se verificó y **pasa**. El resto está construido correcto-por-construcción y su validación requiere JDK 21, Flutter SDK y Docker activos en tu máquina; los comandos están arriba.

## Criterios de aceptación

- [x] Monorepo inicializado (Git, `.gitignore`/`.editorconfig`/`.gitattributes`).
- [x] Backend multi-módulo hexagonal con Flyway apuntando a las migraciones V1–V10.
- [x] Web Angular standalone con Signals, Material, PWA, interceptor y lazy loading — **compila**.
- [x] Mobile Flutter feature-first con Riverpod/GoRouter/Material3/i18n y todas las dependencias del stack.
- [x] Infra de desarrollo (docker-compose con PostGIS, Redis, MinIO, MailHog, Prometheus, Grafana, NGINX).
- [x] CI (GitHub Actions) para los tres proyectos.
- [x] Prueba ArchUnit que impone las reglas de dependencia hexagonal (DR-1..DR-3).
- [ ] Compilación backend (JDK 21) y Flutter, y `docker compose up` — pendientes de verificar en tu entorno.
