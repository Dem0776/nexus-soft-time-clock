# ADR-001 — Modular Monolith + Arquitectura Hexagonal

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
El sistema exige (prompt) Clean Architecture, DDD, Hexagonal, CQRS, Event-Driven y **Modular Monolith con posibilidad de evolucionar a microservicios sin reescribir**. Al inicio no hay carga que justifique la complejidad operativa de microservicios, pero sí se requieren fronteras claras.

## Decisión
Construir un **único deployable** (modular monolith) organizado en **módulos por bounded context**, cada uno con **arquitectura hexagonal** (domain / application / infrastructure) y **regla de dependencia hacia el dominio**. La comunicación entre módulos es por **puertos** (sincrónica, mínima) o por **eventos de dominio** (asíncrona). Las reglas de dependencia se verifican con **ArchUnit**.

## Consecuencias
- ➕ Menor complejidad operativa que microservicios; despliegue y debugging simples.
- ➕ Fronteras explícitas → extracción futura a microservicios sin reescribir el dominio (solo cambiar adaptador de mensajería).
- ➕ Dominio testeable en aislamiento (sin framework).
- ➖ Riesgo de erosión de límites si no se disciplina → mitigado con ArchUnit y (opcional) Spring Modulith.
- ➖ Un solo proceso escala en bloque; aceptable en fase inicial (escalado horizontal de réplicas).
