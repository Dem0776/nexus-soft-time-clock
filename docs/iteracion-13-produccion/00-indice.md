# Iteración 13 — Pruebas, documentación y preparación para producción

**Objetivo:** cerrar el proyecto con pruebas de integración, documentación completa y una checklist de endurecimiento para producción.

## Entregables

| Doc | Contenido |
|---|---|
| [01 — Guía de instalación y desarrollo](01-guia-instalacion-desarrollo.md) | Requisitos, arranque local, comandos por proyecto |
| [02 — Guía de despliegue](02-guia-despliegue.md) | Docker Compose, Kubernetes, variables de entorno, CI/CD |
| [03 — Manual de usuario (colaborador)](03-manual-usuario.md) | Uso de la app móvil |
| [04 — Manual del administrador](04-manual-administrador.md) | Portal: empresas, usuarios, centros, geocercas, reportes |
| [05 — Estrategia de pruebas](05-estrategia-pruebas.md) | Pirámide de pruebas, cobertura, Testcontainers |
| [06 — Checklist de producción](06-checklist-produccion.md) | Endurecimiento de seguridad, observabilidad, escalado |
| [07 — Manual de despliegue en Portainer](07-despliegue-portainer.md) | Stack compose, métodos Git/registry, variables, verificación |

## Pruebas añadidas
- **Testcontainers**: `ApplicationSmokeIT` levanta la app contra **PostGIS real**, ejecuta las migraciones V1–V12 y verifica el arranque + endpoints públicos.
- **JaCoCo**: reporte de cobertura por módulo (objetivo del proyecto ≥ 80%, RNF-13).
- **ArchUnit**: reglas de dependencia hexagonal (desde la Iteración 4).
- Pruebas unitarias de la lógica de negocio en cada módulo (identity, tenancy, geofencing, attendance, sync, incidents, reporting, notifications) — todas verdes en el entorno de generación (compiladas con `release=17`).

## Estado de verificación (final)

| Capa | Verificado en generación | Pendiente (requiere entorno) |
|---|---|---|
| Backend (16 módulos) | ✅ compila; ✅ unit tests verdes | `mvn verify` con **JDK 21** + IT Testcontainers (Docker) |
| Web (Angular) | ✅ `npm run build` | pruebas Karma/Cypress en CI |
| Móvil (Flutter) | ⚠️ no verificable (sin SDK) | `flutter test` + build_runner en CI |
| Infra | ✅ archivos correctos | `docker compose up` / despliegue K8s |

> El único bloqueo del entorno de generación fue la ausencia de **JDK 21**, **Flutter SDK** y **Docker activo**. Todo lo verificable con las herramientas disponibles (compilación backend con release 17, pruebas unitarias, build de Angular) **pasa**. La verificación en runtime está lista para ejecutarse con `mvn verify` y `docker compose up` en un entorno completo (ver guías).

## Resumen del proyecto

13 iteraciones completadas. Sistema multi-tenant de control de asistencia con:
- **Backend** modular monolith hexagonal (16 módulos, event-driven + CQRS + Outbox), Java 21 / Spring Boot 3.3, PostgreSQL/PostGIS, Redis, JWT+refresh, RBAC.
- **Portal** Angular (standalone, Signals, Material, PWA) — auth, administración, dashboards.
- **App** Flutter offline-first (Riverpod, GoRouter, Drift, Dio) — registro QR+GPS y cola de sincronización.
- **Infra** Docker/K8s-ready, NGINX, MinIO, Prometheus/Grafana, GitHub Actions.

Trazabilidad completa RF↔HU↔RN↔CU↔BC en [`docs/iteracion-01-analisis/09-matriz-trazabilidad.md`](../iteracion-01-analisis/09-matriz-trazabilidad.md).
