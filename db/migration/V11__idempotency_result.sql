-- =====================================================================
-- V11 — Resultado idempotente del registro de asistencia
-- Guarda la respuesta previa para devolverla ante reenvíos del mismo
-- operation_uuid sin reprocesar (ADR-004).
-- =====================================================================
ALTER TABLE idempotency_keys
    ADD COLUMN IF NOT EXISTS result_json jsonb;
