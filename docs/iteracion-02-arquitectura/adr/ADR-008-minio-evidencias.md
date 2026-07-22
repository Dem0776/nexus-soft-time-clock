# ADR-008 — MinIO (S3) para evidencias fotográficas

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
Los registros pueden llevar evidencia fotográfica opcional (RF-18); guardar binarios en PostgreSQL es ineficiente y encarece backups.

## Decisión
Almacenar evidencias en **MinIO** (S3-compatible, portable a S3 gestionado). En la BD solo se guarda la **referencia** (bucket/key + metadatos + hash). Acceso mediante **URLs prefirmadas** de vida corta; objetos **cifrados en reposo** (SSE) y por tenant (prefijo/bucket). Subida directa cliente→MinIO con URL firmada cuando convenga, para no cargar el backend.

## Consecuencias
- ➕ Escalable y barato para binarios; BD ligera.
- ➕ Portable a AWS S3/GCS sin cambiar código (API S3).
- ➕ URLs firmadas → control de acceso temporal y auditable.
- ➖ Componente de infraestructura adicional a operar/respaldar.
- ➖ Requiere ciclo de vida/retención de objetos y limpieza de huérfanos.
