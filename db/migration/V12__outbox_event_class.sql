-- =====================================================================
-- V12 — Clase del evento en el outbox
-- Permite al relay deserializar el payload al tipo de evento concreto
-- para republicarlo en el bus interno (Transactional Outbox, ADR-005).
-- =====================================================================
ALTER TABLE outbox_events
    ADD COLUMN IF NOT EXISTS event_class text;
