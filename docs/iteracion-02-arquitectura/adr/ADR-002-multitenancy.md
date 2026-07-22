# ADR-002 — Multi-tenant por columna `tenant_id`

**Estado:** Aceptado · **Fecha:** 2026-07-21 · **Confirmada por el usuario** (default D-01)

## Contexto
La plataforma es multi-empresa (RF-13) con miles de usuarios y millones de registros. Opciones: (a) columna discriminadora `tenant_id`, (b) schema por tenant, (c) base de datos por tenant.

## Decisión
Aislamiento por **columna `tenant_id`** en todas las tablas de negocio. El `tenant_id` se **deriva del token/contexto de seguridad** (RN-31), nunca de parámetros del cliente. Se aplica un **filtro automático** (Hibernate `@Filter` / interceptor) para que ninguna query omita el tenant; opcionalmente **PostgreSQL Row-Level Security (RLS)** como segunda barrera. Índices compuestos con `tenant_id` como prefijo.

## Consecuencias
- ➕ Operación y despliegue simples; una sola BD; migraciones únicas.
- ➕ Escala a muchos tenants sin proliferar esquemas.
- ➕ Facilita reporting cross-tenant para SUPER_ADMIN.
- ➖ El aislamiento es responsabilidad de la aplicación → se refuerza con filtro global + RLS + pruebas de fuga de tenant.
- ➖ Un tenant muy grande comparte tablas → mitigado con **particionamiento** por `tenant_id`/fecha.
- 🔄 Si un cliente exige aislamiento físico (regulatorio), se puede migrar ese tenant a schema/DB dedicada sin cambiar el modelo de dominio.
