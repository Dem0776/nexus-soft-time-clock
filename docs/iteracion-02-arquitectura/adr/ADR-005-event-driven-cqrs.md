# ADR-005 — Event-Driven + CQRS + Transactional Outbox

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
Auditoría, notificaciones, dashboards/reportes y tiempo real no deben acoplar al núcleo de asistencia. Se requiere consistencia entre el cambio de estado y la publicación de eventos, y lecturas eficientes sobre millones de registros (RNF-01, RNF-03).

## Decisión
Integración interna por **eventos de dominio**. Para evitar el problema de doble escritura, se usa **Transactional Outbox**: el agregado y el evento se persisten en la misma transacción; un **relay** publica al bus (in-process ahora, broker al migrar). Los consumidores son **idempotentes** (entrega at-least-once). Se aplica **CQRS**: escritura sobre agregados JPA; lectura desde **read-models** proyectados por eventos.

## Consecuencias
- ➕ Bajo acoplamiento; extensible sin tocar el emisor.
- ➕ Consistencia garantizada (outbox) sin transacciones distribuidas.
- ➕ Lecturas rápidas y escalables (read-models).
- ➕ Camino directo a microservicios (cambiar bus in-process por Kafka/RabbitMQ).
- ➖ Complejidad adicional (outbox, proyectores, idempotencia de consumidores).
- ➖ Consistencia **eventual** en las vistas de lectura → aceptable para dashboards/reportes.
