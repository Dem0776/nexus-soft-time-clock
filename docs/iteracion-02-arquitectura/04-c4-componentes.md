# 04 — C4 Nivel 3: Componentes del Backend API

Descompone el contenedor **Backend API** en sus módulos (bounded contexts) y la plataforma compartida. Cada módulo es hexagonal (adapters → application → domain).

```mermaid
C4Component
    title Backend API — Componentes / Módulos (C4 L3)

    Container_Boundary(api, "Backend API (Spring Boot)") {

        Component(web, "Web Layer", "Spring Web / WebSocket", "REST controllers versionados (/api/v1), manejo uniforme de errores, OpenAPI")

        Component(iam, "Identity & Access (BC-01)", "Módulo hexagonal", "Login, JWT, refresh rotatorio, RBAC, device binding")
        Component(tenancy, "Tenancy (BC-02)", "Módulo hexagonal", "Empresas/tenants, resolución de tenant por token")
        Component(org, "Organization (BC-03)", "Módulo hexagonal", "Centros de trabajo y proyectos")
        Component(sched, "Scheduling (BC-04)", "Módulo hexagonal", "Horarios, turnos, tolerancias")
        Component(geo, "Geofencing (BC-05)", "Módulo hexagonal", "Geocercas, QR firmado, validación espacial (PostGIS)")
        Component(att, "Attendance (BC-06) — CORE", "Módulo hexagonal", "Registro y validación de asistencia")
        Component(fraud, "Anti-Fraud (BC-07)", "Módulo hexagonal", "Evaluación de banderas y políticas antifraude")
        Component(sync, "Offline Sync (BC-08)", "Módulo hexagonal", "Ingesta idempotente y resolución de conflictos")
        Component(inc, "Incidents (BC-09)", "Módulo hexagonal", "Incidencias y justificaciones")
        Component(audit, "Audit (BC-10)", "Módulo hexagonal", "Bitácora inmutable (event-driven)")
        Component(report, "Reporting (BC-11)", "Módulo hexagonal", "Read-models, dashboards, export Excel/PDF/CSV")
        Component(notif, "Notifications (BC-12)", "Módulo hexagonal", "Push (FCM) + email")
        Component(mon, "Monitoring/Realtime (BC-13)", "Módulo", "Métricas, proyecciones tiempo real vía WebSocket")

        Component(shared, "Shared Kernel / Platform", "Librería común", "Event bus + Outbox, seguridad, multitenancy, persistencia base, errores, tipos VO comunes (Geo, Money, Ids)")
    }

    ContainerDb(pg, "PostgreSQL + PostGIS", "DB")
    ContainerDb(redis, "Redis", "Caché/nonce")
    Container(minio, "MinIO", "Evidencias")

    Rel(web, iam, "Autentica/autoriza")
    Rel(web, att, "Comandos de registro")
    Rel(web, sync, "Ingesta offline por lote")
    Rel(web, report, "Consultas/exports")
    Rel(web, org, "CRUD administrativo")
    Rel(web, sched, "CRUD horarios")
    Rel(web, geo, "CRUD geocercas / QR")

    Rel(att, geo, "Valida geocerca + QR", "puerto")
    Rel(att, sched, "Valida horario/turno", "puerto")
    Rel(att, fraud, "Evalúa banderas", "puerto")
    Rel(sync, att, "Delega validación (idempotente)")

    Rel(att, shared, "Publica AttendanceRegistered", "event bus/outbox")
    Rel(audit, shared, "Consume eventos de escritura")
    Rel(notif, shared, "Consume eventos")
    Rel(report, shared, "Proyecta read-models")
    Rel(mon, shared, "Proyecta a tiempo real")

    Rel(iam, redis, "Tokens revocados / rate limit")
    Rel(geo, pg, "Consultas espaciales (GIST)")
    Rel(att, pg, "Persiste registros")
    Rel(att, minio, "Evidencias (URL firmada)")
    Rel(shared, pg, "Outbox / read-models")
```

## Reglas de interacción entre módulos

1. **Sincrónica (puertos):** cuando Attendance necesita validar (Geofencing, Scheduling, Anti-Fraud), invoca **interfaces (puertos)** publicadas por esos módulos. No accede a sus tablas.
2. **Asincrónica (eventos):** los efectos secundarios (auditoría, notificaciones, read-models, tiempo real) se disparan por **eventos de dominio** vía el bus + **Outbox** transaccional. Esto los hace **desacoplables** a microservicios (RNF-20).
3. **Sin acceso cruzado a datos:** ningún módulo lee las tablas de otro; cada uno posee su esquema/tablas.
4. **Shared Kernel mínimo:** solo tipos verdaderamente comunes (identificadores, value objects geográficos, contexto de tenant, contratos de eventos). Se evita convertirlo en un "cajón de sastre".

## Web Layer — convenciones API (adelanto de Iteración 4/5)

- Versionado por ruta: `/api/v1/...`
- Manejo uniforme de errores (RFC 7807 `application/problem+json`).
- Paginación/orden/filtros estandarizados (`?page=&size=&sort=&filter=`).
- DTOs de entrada validados (Spring Validation) y mappers **MapStruct**.
- Documentación **OpenAPI/Swagger UI** autogenerada.
