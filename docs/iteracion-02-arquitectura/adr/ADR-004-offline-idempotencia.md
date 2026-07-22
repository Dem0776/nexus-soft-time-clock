# ADR-004 — Offline-first e idempotencia por UUID de operación

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
La app debe operar sin conexión sin perder registros (RF-21, RNF-10) y evitar duplicados/replay al reenviar (RN-26, RN-51).

## Decisión
Cada operación de registro se crea en el cliente con un **UUID v4** persistido localmente (Drift) **antes** de intentar enviarse. El UUID viaja como **`Idempotency-Key`**. El backend mantiene un **registro de idempotencia** (Redis + tabla) por UUID: si llega un UUID ya procesado, **devuelve el resultado previo** sin duplicar. El `nonce` del QR se marca como consumido para prevenir replay. La sincronización usa **reintentos con backoff exponencial** y un límite antes de marcar error.

## Consecuencias
- ➕ Cero pérdida de registros; reenvíos seguros.
- ➕ Anti-replay y anti-duplicado unificados por diseño.
- ➕ Base clara para resolución de conflictos (servidor autoritativo, RN-53).
- ➖ Requiere almacenar claves de idempotencia con TTL adecuado (memoria/tabla) y limpiar por retención.
- ➖ El cliente debe gestionar estados (`pendiente/sincronizado/rechazado`) y la cola FIFO.
