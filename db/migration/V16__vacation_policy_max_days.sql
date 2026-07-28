-- =====================================================================
-- V16 — Tope de días de vacaciones acumulables
-- Añade un techo a la acumulación por antigüedad. El saldo se calcula como
--   min(days_per_year * años_de_antiguedad, max_days)   (0 = sin tope).
-- Ej.: days_per_year=4, max_days=12 → 1 año=4, 2 años=8, 3 años=12, 4+=12.
-- =====================================================================

ALTER TABLE vacation_policies
    ADD COLUMN IF NOT EXISTS max_days integer NOT NULL DEFAULT 0
    CONSTRAINT ck_vacation_policies_max_days CHECK (max_days >= 0 AND max_days <= 366);

COMMENT ON COLUMN vacation_policies.max_days IS
    'Tope de días acumulables por antigüedad (0 = sin tope). saldo = min(days_per_year * años, max_days).';
