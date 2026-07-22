# ADR-009 — PostgreSQL + PostGIS para geoespacial

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
El núcleo valida ubicación contra geocercas (RN-13) y el prompt exige índices espaciales, consultas geográficas, particionamiento y optimización para millones de registros.

## Decisión
Usar **PostgreSQL** con la extensión **PostGIS**. Las ubicaciones se modelan como `geography(Point,4326)`; las geocercas circulares con centro + radio (y polígonos `geography(Polygon,4326)` a futuro). Validación de pertenencia con `ST_DWithin` / `ST_Contains`. Índices **GIST** sobre columnas geográficas. Tablas de asistencia y auditoría **particionadas** por rango de fecha (+ tenant) para escalar (RNF-03).

## Consecuencias
- ➕ Consultas espaciales correctas (distancia geodésica) y rápidas (GIST).
- ➕ Una sola tecnología para relacional + geoespacial (menos piezas).
- ➕ Particionamiento nativo para grandes volúmenes.
- ➖ PostGIS añade complejidad de instalación (imagen `postgis/postgis`) y en migraciones.
- ➖ Requiere cuidado con índices en tablas particionadas.
