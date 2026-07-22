# 01 — Visión de arquitectura

## 1.1 Estilo arquitectónico

Nexus Soft Time Clock se construye como **Modular Monolith** con **Arquitectura Hexagonal (Ports & Adapters)** por módulo, guiado por **Domain-Driven Design**. Cada bounded context (Iteración 1, doc 08) es un **módulo** con fronteras explícitas; la comunicación entre módulos es preferentemente **por eventos de dominio** (Event-Driven) para lograr bajo acoplamiento y permitir la **extracción a microservicios sin reescritura** (RNF-20).

```
┌──────────────────────────────────────────────────────────────┐
│                     MODULAR MONOLITH (1 deployable)            │
│                                                               │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌─────────┐ │
│  │ Attendance │  │ Geofencing │  │ Scheduling │  │  Audit  │ │
│  │  (hexag.)  │  │  (hexag.)  │  │  (hexag.)  │  │(hexag.) │ │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘  └────┬────┘ │
│        │  eventos de dominio (in-process bus / outbox)  │     │
│        └──────────────────┴────────────────┴───────────┘     │
│  ┌──────────── plataforma compartida ─────────────────────┐  │
│  │ seguridad · multitenancy · persistencia · mensajería   │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

**¿Por qué no microservicios desde el día 1?** Ver [ADR-001](adr/ADR-001-modular-monolith-hexagonal.md): reducir complejidad operativa inicial manteniendo fronteras que permitan dividir después.

## 1.2 Capas de la arquitectura hexagonal (por módulo)

```
        ┌─────────────────── adapters (in) ───────────────────┐
        │   REST controllers · WebSocket · consumers de eventos │
        └───────────────────────────┬──────────────────────────┘
                                     │ (puertos de entrada / use cases)
        ┌────────────────────── application ────────────────────┐
        │  Use Cases (commands/queries) · orquestación · DTOs   │
        └───────────────────────────┬──────────────────────────┘
                                     │ (puertos de salida)
        ┌──────────────────────── domain ──────────────────────┐
        │  Agregados · Entidades · Value Objects · Domain       │
        │  Services · Domain Events · reglas de negocio (RN)    │
        │  ── SIN dependencias de framework ──                  │
        └───────────────────────────┬──────────────────────────┘
                                     │
        ┌────────────────────── adapters (out) ─────────────────┐
        │ JPA repositories · Redis · MinIO · Mail · publishers  │
        └───────────────────────────────────────────────────────┘
```

**Regla de dependencia (Dependency Rule):** las dependencias apuntan **hacia el dominio**. `domain` no conoce a `application` ni a `infrastructure`; `application` define **puertos** (interfaces) que la infraestructura implementa. Inversión de dependencias vía Spring DI.

## 1.3 CQRS

- **Command side:** los use cases de escritura operan sobre agregados y persisten en el modelo transaccional (JPA).
- **Query side:** dashboards/reportes/mapa se sirven desde **read-models** (proyecciones) actualizados por eventos, optimizados para lectura. Ver [07 — Modelo de eventos y CQRS](07-modelo-eventos-cqrs.md).

## 1.4 Decisiones transversales (cross-cutting)

| Preocupación | Enfoque | ADR |
|---|---|---|
| Multi-tenant | Columna `tenant_id` derivada del token; filtro automático (Hibernate filter); PostgreSQL RLS opcional | [ADR-002](adr/ADR-002-multitenancy.md) |
| Tiempo | Hora de servidor autoritativa; almacenamiento UTC | [ADR-003](adr/ADR-003-server-time.md) |
| Offline + anti-replay | Idempotencia por UUID de operación (Idempotency-Key) | [ADR-004](adr/ADR-004-offline-idempotencia.md) |
| Integración interna | Eventos de dominio + patrón **Transactional Outbox** | [ADR-005](adr/ADR-005-event-driven-cqrs.md) |
| Seguridad QR | Token firmado (HMAC/JWT) con `nonce` + vigencia | [ADR-006](adr/ADR-006-qr-firmado.md) |
| AuthN/AuthZ | JWT access corto + refresh rotatorio; RBAC | [ADR-007](adr/ADR-007-jwt-refresh.md) |
| Evidencias | MinIO (S3) con URLs firmadas | [ADR-008](adr/ADR-008-minio-evidencias.md) |
| Geoespacial | PostgreSQL + PostGIS (índices GIST) | [ADR-009](adr/ADR-009-postgis.md) |
| Caché / rate limit | Redis | [ADR-010](adr/ADR-010-redis.md) |
| Tiempo real | Spring WebSocket (STOMP) | [ADR-011](adr/ADR-011-websocket-realtime.md) |

## 1.5 Principios de código (recordatorio)

Clean Code · SOLID · DRY · KISS · YAGNI · Effective Java · Effective Dart · Angular Style Guide · convenciones de Spring. Sin código duplicado, obsoleto ni librerías sin mantenimiento. Cobertura ≥ 80% (RNF-13).

## 1.6 Atributos de calidad priorizados

1. **Seguridad e integridad del registro** (antifraude, hora de servidor) — es la razón de ser del producto.
2. **Resiliencia offline** (cero pérdida de registros).
3. **Escalabilidad** (miles de concurrentes, millones de registros).
4. **Mantenibilidad/evolutividad** (módulos desacoplados).
5. **Observabilidad** (métricas, trazas, auditoría).
