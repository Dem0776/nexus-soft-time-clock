-- =====================================================================
-- V15 — QR de centro de vigencia larga: reutilizable, una vez por usuario y día (RN-26 extendida)
-- =====================================================================
-- Antes: un nonce se consumía una única vez para todo el tenant (QR de un solo uso total).
-- Ahora que el QR admite vigencias de días/semanas/meses, debe poder ser escaneado por
-- distintos colaboradores, y por el mismo colaborador en días distintos, mientras siga vigente.
-- La barrera anti-abuso pasa a ser "una vez por usuario y por día calendario (UTC)".

ALTER TABLE qr_nonce_consumed
    ADD COLUMN user_id     uuid,
    ADD COLUMN consumed_on date NOT NULL DEFAULT (timezone('utc', now()))::date;

ALTER TABLE qr_nonce_consumed ALTER COLUMN consumed_on DROP DEFAULT;

DROP INDEX uq_qr_nonce_consumed;
CREATE UNIQUE INDEX uq_qr_nonce_consumed ON qr_nonce_consumed (tenant_id, nonce, user_id, consumed_on);

COMMENT ON COLUMN qr_nonce_consumed.user_id IS
    'Usuario que consumió el nonce ese día. Junto con consumed_on delimita "una vez por persona y día" para QR de vigencia larga.';
COMMENT ON COLUMN qr_nonce_consumed.consumed_on IS
    'Fecha (UTC) del consumo. Filas previas a esta migración quedan con user_id NULL (Postgres las trata como no-conflictivas entre sí).';
