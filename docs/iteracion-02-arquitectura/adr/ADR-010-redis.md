# ADR-010 — Redis para caché, rate limiting y anti-replay

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
Se necesita caché (Spring Cache), rate limiting (RNF-07), control de `nonce`/idempotencia para anti-replay (RN-26), lista de tokens revocados y coordinación de jobs en multi-réplica.

## Decisión
Usar **Redis** como store en memoria para: (1) caché de lecturas costosas; (2) **rate limiting** por IP/usuario (token bucket); (3) claves de **idempotencia** y **nonce consumidos** con TTL; (4) **blacklist** de refresh/access revocados; (5) **locks** de scheduler (ShedLock); (6) presencia/estado para tiempo real. Backend **stateless** apoyado en Redis para escalar horizontalmente.

## Consecuencias
- ➕ Baja latencia; habilita escalado horizontal sin estado local.
- ➕ Unifica varias necesidades transversales en una pieza probada.
- ➖ Punto adicional a operar y monitorear (HA/persistencia según criticidad).
- ➖ TTLs y claves deben diseñarse con cuidado (memoria acotada).
