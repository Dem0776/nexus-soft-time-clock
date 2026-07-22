# Iteración 6 — Empresas, usuarios, roles y centros de trabajo

**Objetivo:** CRUD administrativo sobre tres bounded contexts, con RBAC por permiso, multi-tenant, paginación/filtros y validación.

## Backend (nuevos módulos y extensiones)

### Tenancy (BC-02) — `backend/modules/tenancy/`
Gestión de empresas (SUPER_ADMIN, permiso `company:manage`).
- `POST/GET/PUT /api/v1/companies`, `GET /{id}`, `PATCH /{id}/status`
- Alta crea la empresa **y** su `company_settings` por defecto. Código único.

### Organization (BC-03) — `backend/modules/organization/`
Centros de trabajo (`worksite:manage`) y proyectos (`project:manage`), **acotados al tenant** del token.
- `/api/v1/work-sites` — con **ubicación PostGIS** (lat/lng → `geography(Point,4326)` vía hibernate-spatial/JTS) y overrides de política (precisión, foto, biometría).
- `/api/v1/projects` — con vigencia y estado.

### Identity (BC-01) — gestión de usuarios y roles
- `/api/v1/users` (`user:manage`): crear (con hash BCrypt + roles), listar, obtener, cambiar estado, **asignar roles**.
- `/api/v1/roles` (`role:manage`): listar roles plantilla del sistema.

Todos los listados soportan `?page=&size=&search=`; respuestas con envelope uniforme `PageResponse`. Errores 404 (`ResourceNotFoundException`) y 422 (reglas) con ProblemDetail.

## Web (Angular)
- `features/admin/companies`: servicio REST + tabla Material + alta (lazy en `/admin/companies`).

## Estado de verificación

| Qué | Cómo | Resultado |
|---|---|---|
| **Backend compila** | `mvn compile` (reactor completo, release 17) | ✅ exit 0 |
| **Lógica de negocio** | `mvn test` (identity + tenancy, JUnit+Mockito) | ✅ tests pasan |
| **Portal Angular** | `npm run build` | ✅ exit 0 (`companies-component`) |
| Runtime E2E | stack + JDK 21 + Docker | ⚠️ no ejecutado |

> Nota PostGIS: el mapeo `Point`↔`geography` con hibernate-spatial está construido correcto-por-construcción; su lectura/escritura real contra PostGIS se validará al levantar el stack (JDK 21 + Docker).

## Criterios de aceptación
- [x] CRUD de empresas (super admin) con configuración por defecto.
- [x] CRUD de centros de trabajo geolocalizados (PostGIS) y proyectos, tenant-scoped.
- [x] Gestión de usuarios (alta con roles, estado) y listado de roles.
- [x] RBAC por permiso (`@PreAuthorize`), paginación/filtro/orden, validación, errores uniformes.
- [x] Slice de portal Angular (empresas) compilando.
- [ ] Verificación E2E en runtime (pendiente JDK 21 + Docker).
