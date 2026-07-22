# 03 — C4 Nivel 2: Diagrama de Contenedores

Descompone el sistema en unidades desplegables (contenedores) y su comunicación.

```mermaid
C4Container
    title Nexus Soft Time Clock — Diagrama de Contenedores (C4 L2)

    Person(employee, "Colaborador", "App móvil")
    Person(webuser, "Supervisor / Admin", "Portal web")

    System_Boundary(nexus, "Nexus Soft Time Clock") {
        Container(mobile, "App móvil", "Flutter / Dart", "Offline-first (Drift), Riverpod, escaneo QR, GPS, antifraude local, cola de sync")
        Container(spa, "Portal web (PWA)", "Angular / TS", "Dashboards, mapa en tiempo real, administración, reportes")
        Container(gateway, "NGINX / Ingress", "NGINX", "TLS termination, reverse proxy, rate limiting perimetral")
        Container(api, "Backend API", "Java 21 / Spring Boot", "Modular monolith hexagonal: REST, casos de uso, dominio, eventos")
        Container(ws, "Canal tiempo real", "Spring WebSocket (STOMP)", "Push de eventos de asistencia y sync al portal")
        Container(scheduler, "Jobs / Scheduler", "Spring Scheduler", "Rotación de QR, agregaciones, retención, reintentos")
        ContainerDb(pg, "PostgreSQL + PostGIS", "PostgreSQL", "Datos transaccionales, geoespaciales, read-models, auditoría (particionada)")
        ContainerDb(redis, "Redis", "Redis", "Caché, rate limiting, nonce/replay, tokens revocados, presencia")
        Container(minio, "MinIO", "S3-compatible", "Evidencias fotográficas cifradas")
        Container(obs, "Observabilidad", "Prometheus + Grafana", "Métricas y dashboards de monitoreo")
    }

    System_Ext(fcm, "Firebase (FCM/Crashlytics)", "Push y telemetría")
    System_Ext(mail, "SMTP", "Correo")

    Rel(employee, mobile, "Usa")
    Rel(webuser, spa, "Usa", "HTTPS")
    Rel(mobile, gateway, "REST (JWT, Idempotency-Key)", "HTTPS")
    Rel(spa, gateway, "REST + WebSocket", "HTTPS/WSS")
    Rel(gateway, api, "Enruta", "HTTP")
    Rel(gateway, ws, "Enruta WSS", "HTTP")

    Rel(api, pg, "Lee/escribe (JPA, queries espaciales)", "JDBC")
    Rel(api, redis, "Caché / rate limit / nonce", "RESP")
    Rel(api, minio, "Guarda/recupera evidencias (URLs firmadas)", "S3")
    Rel(api, ws, "Publica eventos", "in-process")
    Rel(scheduler, pg, "Jobs programados", "JDBC")
    Rel(api, mail, "Envía correos", "SMTP/TLS")
    Rel(mobile, fcm, "Recibe push / reporta", "HTTPS")
    Rel(api, fcm, "Solicita push", "HTTPS")
    Rel(api, obs, "Expone /actuator/prometheus", "HTTP")
```

## Notas de despliegue

- **Backend stateless**: la sesión vive en JWT + Redis; permite **escalado horizontal** detrás de NGINX/Ingress (RNF-02).
- **Scheduler**: en despliegue multi-réplica se coordina con locks (Redis/ShedLock) para no duplicar jobs.
- **PostgreSQL**: tablas de asistencia y auditoría **particionadas** por fecha/tenant (RNF-03); índices **GIST** para geoespacial.
- **MinIO** y **Redis** externalizan estado y binarios fuera del proceso de la app (12-factor).
- Todo el tráfico entra por **NGINX con TLS** (RNF-05); rate limiting perimetral + a nivel de aplicación.

Detalle de los **componentes internos del Backend API** en [04 — C4 Componentes](04-c4-componentes.md).
