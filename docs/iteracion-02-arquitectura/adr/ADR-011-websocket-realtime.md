# ADR-011 — WebSocket (STOMP) para tiempo real

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
El portal requiere **mapa y dashboards en tiempo real** y estado de sincronización (RF-25, RF-26, CU-11).

## Decisión
Usar **Spring WebSocket con STOMP** para empujar eventos al portal. Los clientes se suscriben a destinos por **tenant/ámbito** (`/topic/tenant/{id}/...`) con autorización por JWT en el handshake y filtrado por permisos/ámbito del supervisor (RN-33). Los eventos provienen del **event bus** (proyecciones de `AttendanceRegistered`, `SyncBatchProcessed`, etc.). En multi-réplica se usa un **broker relay** (Redis pub/sub o broker externo) para difundir entre instancias.

## Consecuencias
- ➕ Actualizaciones push de baja latencia sin polling.
- ➕ Integra naturalmente con el modelo event-driven.
- ➖ Estado de conexiones y escalado entre réplicas requiere relay (Redis/broker).
- ➖ Autorización por destino debe implementarse con cuidado (fuga entre tenants).
- 🔄 Alternativa SSE descartada por necesidad de suscripciones segmentadas y bidireccionalidad futura.
