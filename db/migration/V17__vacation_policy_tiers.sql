-- =====================================================================
-- V17 — Escalera de vacaciones por antigüedad (tramos configurables)
-- Reemplaza el modelo "días/año + tope" por una tabla de tramos
-- (año de antigüedad → días) más una regla para los años posteriores al
-- último tramo: FLAT (se mantiene) o INCREMENT (+N días cada M años).
-- Las columnas days_per_year y max_days (V15/V16) quedan obsoletas pero
-- se conservan por compatibilidad.
--   entitlement(años) = tramo aplicable; más allá del último → regla beyond.
-- =====================================================================

ALTER TABLE vacation_policies
    ADD COLUMN IF NOT EXISTS tiers                 text        NOT NULL DEFAULT '[]',
    ADD COLUMN IF NOT EXISTS beyond_mode           varchar(10) NOT NULL DEFAULT 'FLAT',
    ADD COLUMN IF NOT EXISTS beyond_increment_days integer     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS beyond_every_years    integer     NOT NULL DEFAULT 1;

ALTER TABLE vacation_policies
    ADD CONSTRAINT ck_vacation_policies_beyond_mode CHECK (beyond_mode IN ('FLAT','INCREMENT'));
ALTER TABLE vacation_policies
    ADD CONSTRAINT ck_vacation_policies_beyond_every CHECK (beyond_every_years >= 1);

-- Semilla LFT (MX) para las políticas existentes que aún no tienen escalera:
-- 12 días el 1er año, +2/año hasta 20 al 5º, luego +2 cada 5 años.
UPDATE vacation_policies
   SET tiers = '[{"year":1,"days":12},{"year":2,"days":14},{"year":3,"days":16},{"year":4,"days":18},{"year":5,"days":20}]',
       beyond_mode = 'INCREMENT',
       beyond_increment_days = 2,
       beyond_every_years = 5
 WHERE tiers = '[]';

COMMENT ON COLUMN vacation_policies.tiers IS
    'Escalera año→días como JSON, p.ej. [{"year":1,"days":5},{"year":2,"days":8}].';
COMMENT ON COLUMN vacation_policies.beyond_mode IS
    'Años posteriores al último tramo: FLAT (se mantiene) o INCREMENT (+N cada M años).';
