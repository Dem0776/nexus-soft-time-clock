# 08 — Bounded Contexts (mapa de contextos)

Identificación de contextos delimitados (DDD) como base para la **modularización** del monolito (Iteración 2) y su eventual extracción a microservicios (RNF-20). Cada contexto será un **módulo** con su propio dominio, application y adaptadores (hexagonal).

| BC | Contexto | Responsabilidad | Agregados principales | Cubre RF |
|---|---|---|---|---|
| **BC-01** | **Identity & Access** | Autenticación, JWT/refresh, RBAC, device binding | User, Role, Permission, RefreshToken, Device | RF-01, RF-22, RF-28 |
| **BC-02** | **Tenancy** | Alta y configuración de empresas; aislamiento multi-tenant | Company (Tenant), TenantSettings | RF-13 |
| **BC-03** | **Organization** | Centros de trabajo y proyectos | WorkSite, Project | RF-07, RF-23 |
| **BC-04** | **Scheduling** | Horarios y turnos con tolerancias | Schedule, Shift, ShiftAssignment | RF-08 |
| **BC-05** | **Geofencing** | Geocercas, QR de centro, validación geoespacial | Geofence, SiteQrToken | RF-10, RF-14 |
| **BC-06** | **Attendance** | Registro y validación de asistencia (núcleo) | AttendanceRecord, WorkDay | RF-02, RF-03, RF-04, RF-05, RF-15, RF-16, RF-17, RF-18, RF-19 |
| **BC-07** | **Anti-Fraud** | Reglas y detección de fraude, evaluación de banderas | FraudPolicy, FraudFlag | RF-20 |
| **BC-08** | **Offline Sync** | Recepción idempotente y resolución de conflictos | SyncOperation, SyncResult | RF-21, RF-26 |
| **BC-09** | **Incidents** | Incidencias y su resolución | Incident, Justification | RF-09 |
| **BC-10** | **Audit** | Bitácora inmutable de acciones | AuditLog | RF-12 |
| **BC-11** | **Reporting** | Consultas de lectura (CQRS) y exportaciones | ReportRequest, read-models | RF-11, RF-24 |
| **BC-12** | **Notifications** | Push/email de eventos relevantes | Notification, Channel | RF-27 |
| **BC-13** | **Monitoring / Realtime** | Métricas, mapa y estado en tiempo real (WebSocket) | — (proyecciones/eventos) | RF-25, RF-26 |

## Relaciones entre contextos (context map — preliminar)

- **Attendance (BC-06)** es el núcleo (core domain). Consume de **Geofencing (BC-05)**, **Scheduling (BC-04)** y **Anti-Fraud (BC-07)** para validar; publica `AttendanceRegistered`.
- **Offline Sync (BC-08)** es el _gateway_ de entrada de registros en campo; delega la validación en Attendance (idempotencia + hora de servidor).
- **Audit (BC-10)**, **Notifications (BC-12)**, **Reporting/Read-models (BC-11)** y **Monitoring (BC-13)** son **consumidores de eventos** (event-driven) → bajo acoplamiento, aptos para extraer como microservicios.
- **Identity & Access (BC-01)** y **Tenancy (BC-02)** son **transversales** (upstream): todos los contextos dependen del contexto de seguridad/tenant.
- **CQRS:** la escritura vive en los contextos de dominio; la lectura de dashboards/reportes se sirve desde **read-models** proyectados por eventos (BC-11).

## Clasificación estratégica (DDD)

| Tipo | Contextos |
|---|---|
| **Core domain** | Attendance (BC-06), Anti-Fraud (BC-07), Geofencing (BC-05), Offline Sync (BC-08) |
| **Supporting** | Scheduling (BC-04), Organization (BC-03), Incidents (BC-09) |
| **Generic** | Identity & Access (BC-01), Tenancy (BC-02), Audit (BC-10), Notifications (BC-12), Reporting (BC-11), Monitoring (BC-13) |

> Este mapa guía la estructura de módulos Maven/Gradle del backend en la Iteración 2 y la organización _feature-first_ de Flutter y _lazy-loaded modules_ de Angular.
